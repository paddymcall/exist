/*
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 *  $Id: MailSessionFunctions.java 9745 2009-08-09 21:37:29Z ixitar $
 */

package org.exist.xquery.modules.xmpp;


import org.apache.log4j.Logger;
import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.IntegerValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * eXist Mail Module Extension GetSession
 * 
 * Get a mail session
 * 
 * @author Andrzej Taramina <andrzej@chaeron.com>
 * @serial 2009-03-12
 * @version 1.3
 *
 * @see org.exist.xquery.BasicFunction#BasicFunction(org.exist.xquery.XQueryContext, org.exist.xquery.FunctionSignature)
 */
public class XMPPLoginFunction extends BasicFunction
{
	protected static final Logger logger = Logger.getLogger(XMPPLoginFunction.class);

	public final static FunctionSignature signatures[] = {
		new FunctionSignature(
			new QName( "login", XMPPModule.NAMESPACE_URI, XMPPModule.PREFIX ),
				"Login to XMPP service.",
				new SequenceType[]
				{
					new FunctionParameterSequenceType( "connection", Type.LONG, Cardinality.EXACTLY_ONE, 
						"The connection handle to login.")
				},
				new FunctionReturnSequenceType(Type.BOOLEAN, Cardinality.EXACTLY_ONE, 
					"true if the login is successful")
			),
			new FunctionSignature(
				new QName( "login", XMPPModule.NAMESPACE_URI, XMPPModule.PREFIX ),
				"Login to XMPP service.",
				new SequenceType[]
				{
					new FunctionParameterSequenceType( "connection", Type.LONG, Cardinality.EXACTLY_ONE, 
						"The connection handle to login."),
					new FunctionParameterSequenceType( "user", Type.STRING, Cardinality.EXACTLY_ONE, 
						"The user's name."),
					new FunctionParameterSequenceType( "password", Type.STRING, Cardinality.EXACTLY_ONE, 
						"The user's password.")
				},
				new FunctionReturnSequenceType(Type.BOOLEAN, Cardinality.EXACTLY_ONE, 
					"true if the login is successful")
			),
			new FunctionSignature(
				new QName( "login", XMPPModule.NAMESPACE_URI, XMPPModule.PREFIX ),
				"Login to XMPP service.",
				new SequenceType[]
				{
					new FunctionParameterSequenceType( "connection", Type.LONG, Cardinality.EXACTLY_ONE, 
						"The connection handle to login."),
					new FunctionParameterSequenceType( "user", Type.STRING, Cardinality.EXACTLY_ONE, 
						"The user's name."),
					new FunctionParameterSequenceType( "password", Type.STRING, Cardinality.EXACTLY_ONE, 
						"The user's password."),
					new FunctionParameterSequenceType( "resorce", Type.STRING, Cardinality.ZERO_OR_ONE, 
						"The XMPP resource name.")
				},
				new FunctionReturnSequenceType(Type.BOOLEAN, Cardinality.EXACTLY_ONE, 
					"true if the login is successful")
			)
		};

	public XMPPLoginFunction( XQueryContext context, FunctionSignature signature )
	{
		super( context, signature );
    }

	public Sequence eval( Sequence[] args, Sequence contextSequence ) throws XPathException
	{
		long connectionHandle = ((IntegerValue) args[0].itemAt(0)).getLong();
		XMPPConnection connection = XMPPModule.retrieveConnection(connectionHandle);
		
		try 
		{
			if (args.length == 1){
				connection.loginAnonymously();
			} 
			else {
				
				String user     = args[1].itemAt(0).getStringValue();
				String password = args[2].itemAt(0).getStringValue();
				String resource = null;
				
				if (args.length > 3){
					Sequence sec = args[3];
					if (!sec.isEmpty()){
						resource = sec.itemAt(0).getStringValue();
					}
				}
				
				if (resource == null){
					connection.login(user, password);
				} else connection.login(user, password, resource);
			}
			
			return BooleanValue.TRUE;
			
		} catch (XMPPException e) {
			return BooleanValue.FALSE;
		}
		
	}
}