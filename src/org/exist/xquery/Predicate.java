
/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04,  Wolfgang M. Meier (meier@ifs.tu-darmstadt.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 *  $Id$
 */
package org.exist.xquery;

import java.util.Iterator;

import org.exist.dom.ArraySet;
import org.exist.dom.ContextItem;
import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentSet;
import org.exist.dom.ExtArrayNodeSet;
import org.exist.dom.NodeProxy;
import org.exist.dom.NodeSet;
import org.exist.dom.VirtualNodeSet;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.NumericValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

/**
 *  Handles predicate expressions.
 *
 *@author     Wolfgang Meier
 */
public class Predicate extends PathExpr {

    private final static int NODE = 0;
    private final static int BOOLEAN = 1;
    private final static int POSITIONAL = 2;
    
	private CachedResult cached = null;
	
	private int executionMode = BOOLEAN;
	
	public Predicate(XQueryContext context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.PathExpr#getDependencies()
	 */
	public int getDependencies() {
		if(getLength() == 1) {
			return getExpression(0).getDependencies();
		} else {
			return super.getDependencies();
        }
	}
	
	/* (non-Javadoc)
     * @see org.exist.xquery.PathExpr#analyze(org.exist.xquery.Expression)
     */
    public void analyze(Expression parent, int flags) throws XPathException {
        flags |= IN_PREDICATE; // set flag to signal subexpression that we are in a predicate
        flags &= ~IN_WHERE_CLAUSE;	// remove where clause flag
        Expression inner = getExpression(0);
        if(inner == null)
            return;
        super.analyze(this, flags);
        // Case 1: predicate expression returns a node set. 
        // Check the returned node set against the context set 
        // and return all nodes from the context, for which the
		// predicate expression returns a non-empty sequence.
        if (Type.subTypeOf(inner.returnsType(), Type.NODE)) {
            if(!Dependency.dependsOn(inner.getDependencies(), Dependency.CONTEXT_ITEM))
                executionMode = NODE;
            else
                executionMode = BOOLEAN;
        // Case 2: predicate expression returns a number.
        } else if (Type.subTypeOf(inner.returnsType(), Type.NUMBER)) {
            executionMode = POSITIONAL;
        // Case 3: predicate expression evaluates to a boolean.
        } else
            executionMode = BOOLEAN;
        
		if(executionMode == BOOLEAN) {
		    flags |= SINGLE_STEP_EXECUTION;
		    // need to re-analyze:
		    super.analyze(parent, flags);
		}
    }
    
	public Sequence evalPredicate(Sequence outerSequence, Sequence contextSequence,	int mode)
		    throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);
            context.getProfiler().message(this, Profiler.DEPENDENCIES, "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT SEQUENCE", contextSequence);
        }            
        
        Sequence result;
		Expression inner = getExpression(0);
		if (inner == null)
			result = Sequence.EMPTY_SEQUENCE;
        else {
            int recomputedExecutionMode = executionMode; 
            
            //TODO : use an else/else if construct -pb

            // just to be sure: change mode to boolean if the predicate expression returns a number
            //TODO : the code, likely to be correct, implements the exact contrary     
            if (executionMode == BOOLEAN && Type.subTypeOf(inner.returnsType(), Type.NUMBER)) {
                recomputedExecutionMode = POSITIONAL;
            }  
            
            if (executionMode == NODE &&
            		!(contextSequence instanceof VirtualNodeSet) && 
            		Type.subTypeOf(contextSequence.getItemType(), Type.ATOMIC)) {
                recomputedExecutionMode = BOOLEAN;
            }
            
    		switch(recomputedExecutionMode) {
    			case NODE: 
                    if (context.getProfiler().isEnabled())
                        context.getProfiler().message(this, Profiler.OPTIMIZATION_FLAGS, 
                                "OPTIMIZATION CHOICE", "selectByNodeSet");                    
                    result = selectByNodeSet(contextSequence);                    
                    break;
                case BOOLEAN: 
                    if (context.getProfiler().isEnabled())
                        context.getProfiler().message(this, Profiler.OPTIMIZATION_FLAGS, 
                                "OPTIMIZATION CHOICE", "evalBoolean");                    
                    result = evalBoolean(contextSequence, inner);
                    break;
    			case POSITIONAL: 
                    if (context.getProfiler().isEnabled())
                        context.getProfiler().message(this, Profiler.OPTIMIZATION_FLAGS, 
                                "OPTIMIZATION CHOICE", "selectByPosition");                    
                    result = selectByPosition(outerSequence, contextSequence, mode, inner);
                    break;
    			default:
                    throw new IllegalArgumentException("Unsupported execution mode: '" + recomputedExecutionMode + "'");			    
    		}            
        }
        
        if (context.getProfiler().isEnabled()) 
            context.getProfiler().end(this, "", result);
        
        return result;

	}

	/**
	 * @param contextSequence
	 * @param inner
	 * @return
	 * @throws XPathException
	 */
	private Sequence evalBoolean(Sequence contextSequence, Expression inner) throws XPathException {
		Sequence result = new ValueSequence();
		int p = 0;
		for (SequenceIterator i = contextSequence.iterate(); i.hasNext(); p++) {
			Item item = i.nextItem();
            context.setContextPosition(p); 
            Sequence innerSeq;            
            innerSeq = inner.eval(contextSequence, item);
			if(innerSeq.effectiveBooleanValue())
				result.add(item);
		}
		return result;
	}

	/**
	 * @param contextSequence
	 * @return
	 * @throws XPathException
	 */
	private Sequence selectByNodeSet(Sequence contextSequence) throws XPathException {
		ExtArrayNodeSet result = new ExtArrayNodeSet();
		NodeSet contextSet = contextSequence.toNodeSet();
		boolean contextIsVirtual = contextSet instanceof VirtualNodeSet;
		NodeSet nodes =	super.eval(contextSet, null).toNodeSet();
		
		/* if the predicate expression returns results from the cache
		 * we can also return the cached result. 
		 */
		if(cached != null && cached.isValid(contextSequence) && nodes.isCached()) {
            if (context.getProfiler().isEnabled())                     
                context.getProfiler().message(this, Profiler.OPTIMIZATIONS, 
                        "Using cached results", result);
            return cached.getResult();
        }
       
		DocumentImpl lastDoc = null;      
		for (Iterator i = nodes.iterator(); i.hasNext();) {               
            NodeProxy currentNode = (NodeProxy) i.next();
            int sizeHint = -1;
			if(lastDoc == null || currentNode.getDocument() != lastDoc) {
				lastDoc = currentNode.getDocument();
				sizeHint = nodes.getSizeHint(lastDoc);
			}
            ContextItem contextItem = currentNode.getContext();
			if (contextItem == null) {
				throw new XPathException("Internal evaluation error: context is missing for node " +
                        currentNode.getGID() + " !");
			}
           
            int count = 0;
			while (contextItem != null) { 
                NodeProxy next = contextItem.getNode();
				if(contextIsVirtual || contextSet.contains(next)) {
                    //if (count ==  contextMark) {    
                        next.addMatches(currentNode);
                        result.add(next, sizeHint);
                        //break;
                    //}                     
				}
                contextItem = contextItem.getNextItem();
                count++;
			}
		}
        
		if (contextSequence instanceof NodeSet)
			cached = new CachedResult((NodeSet)contextSequence, result);
        
		return result;
	}

	/**
	 * @param outerSequence
	 * @param contextSequence
	 * @param mode
	 * @param inner
	 * @return
	 * @throws XPathException
	 */
	private Sequence selectByPosition(Sequence outerSequence, Sequence contextSequence, int mode, Expression inner) throws XPathException {
		if(Type.subTypeOf(contextSequence.getItemType(), Type.NODE) && outerSequence != null &&
			outerSequence.getLength() > 0) {
			Sequence result = new ExtArrayNodeSet(100);
			NodeSet contextSet = contextSequence.toNodeSet();
            switch(mode) {
            case Constants.CHILD_AXIS:
            case Constants.ATTRIBUTE_AXIS:
            case Constants.DESCENDANT_AXIS:
            case Constants.DESCENDANT_SELF_AXIS:
            case Constants.DESCENDANT_ATTRIBUTE_AXIS: 
            {                
                outerSequence.clearContext();
				Sequence ancestors = contextSet.selectAncestorDescendant(outerSequence.toNodeSet(),
						NodeSet.ANCESTOR, true, true);
				ArraySet temp = new ArraySet(100);
				for(SequenceIterator i = ancestors.iterate(); i.hasNext(); ) {					
				    NodeProxy p = (NodeProxy)i.nextItem();
				    ContextItem contextNode = p.getContext();
				    temp.reset();
				    while (contextNode != null) {
				    	temp.add(contextNode.getNode());
				    	contextNode = contextNode.getNextItem();
				    }
                    //TODO : understand why we sort here...
				    temp.sortInDocumentOrder();
				    
				    Sequence innerSeq = inner.eval(contextSequence);                    
				    for(SequenceIterator j = innerSeq.iterate(); j.hasNext(); ) {				      
				        NumericValue v = (NumericValue)j.nextItem().convertTo(Type.NUMBER);
				        //... whereas we don't want a sorted array here
                        //TODO : rename this method as getInDocumentOrder ? -pb
				        p = temp.getUnsorted(v.getInt() - 1);
				        if (p != null)
				        	result.add(p);
                        //TODO : does null make sense here ?
				    }
				}
                break;
            }
            default:
				for (SequenceIterator i = outerSequence.iterate(); i.hasNext(); ) {				   
				    NodeProxy p = (NodeProxy)i.nextItem();
				    Sequence temp;
                    boolean reverseAxis = true;
                    switch(mode) {
                    case Constants.ANCESTOR_AXIS:                            
                        temp = contextSet.selectAncestors(p, false, false);
                        break;
                    case Constants.ANCESTOR_SELF_AXIS:
                       temp = contextSet.selectAncestors(p, true, false);
                        break;
                    case Constants.PARENT_AXIS:
                        temp = p.getParents(false);
                        break;                            
                    case Constants.PRECEDING_AXIS:
                        temp = contextSet.selectPreceding(p);
                        break;
                    case Constants.PRECEDING_SIBLING_AXIS:
                        temp = contextSet.selectPrecedingSiblings(p, true);
                        break;
                    case Constants.FOLLOWING_SIBLING_AXIS:
                        temp = contextSet.selectFollowingSiblings(p, true);
                        reverseAxis = false;
                        break;    
                    case Constants.FOLLOWING_AXIS:
                        temp = contextSet.selectFollowing(p);
                        reverseAxis = false;
                        break;
                    case Constants.SELF_AXIS:
                        temp = p;
                        reverseAxis = false;
                        break;                            
                    default:                            
                        //temp = contextSet.selectAncestorDescendant(p, NodeSet.DESCENDANT, false, false);
                        //break;
                        throw new IllegalArgumentException("Tested unknown axis");
                    }
                    if (temp.getLength() > 0) {
                        //TODO : build a value sequence *one* time ? -pb
                        Sequence innerSeq = inner.eval(contextSequence);                        
                        for(SequenceIterator j = innerSeq.iterate(); j.hasNext(); ) {                    
                            NumericValue v = (NumericValue)j.nextItem().convertTo(Type.NUMBER);                             
                            int pos = (reverseAxis ? temp.getLength() - v.getInt() : v.getInt());
                            //Other positions are ignored    
        				    if (pos >= 0 && pos < temp.getLength()) {                                 
    				            result.add(temp.itemAt(pos));
                            }                       
    				    }
                    }
    			}
			}
			return result;
		} else {   
            //TODO : reconsider. If the fallback is confirmed, log it to the profiler
            
            /*
            ValueSequence result = new ValueSequence();
			Sequence innerSeq = inner.eval(contextSequence);			
			for(SequenceIterator i = innerSeq.iterate(); i.hasNext(); ) {
				NumericValue v = (NumericValue)i.nextItem().convertTo(Type.NUMBER);
				int pos = v.getInt() - 1;
                //Other positions are ignored    
				if(pos >= 0 && pos < contextSequence.getLength())
					result.add(contextSequence.itemAt(pos));                            
			}
			return result;
            */
        
            return evalBoolean(contextSequence, inner);
		}
	}
	
	public void setContextDocSet(DocumentSet contextSet) {
		super.setContextDocSet(contextSet);
		if (getLength() > 0)
			getExpression(0).setContextDocSet(contextSet);
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xquery.PathExpr#resetState()
	 */
	public void resetState() {       
		super.resetState();
		cached = null;		
	}
}
