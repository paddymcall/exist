/*
	Copyright (c) 2004-2009, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/


if(!dojo._hasResource["betterform.ui.select1.RadioGroup"]){dojo._hasResource["betterform.ui.select1.RadioGroup"]=true;dojo.provide("betterform.ui.select1.RadioGroup");dojo.require("dijit._Widget");dojo.require("betterform.ui.ControlValue");dojo.declare("betterform.ui.select1.RadioGroup",betterform.ui.ControlValue,{radioItems:null,buildRendering:function $DA4q_(){this.domNode=this.srcNodeRef;},postMixInProperties:function $DA4r_(){this.inherited(arguments);this.applyProperties(dijit.byId(this.xfControlId),this.srcNodeRef);if(dojo.attr(this.srcNodeRef,"incremental")==undefined||dojo.attr(this.srcNodeRef,"incremental")==""||dojo.attr(this.srcNodeRef,"incremental")=="true"){this.incremental=true;}else{this.incremental=false;}},postCreate:function $DA4s_(){var _1=this.id;dojo.query("*[controltype='radioButtonEntry']",this.domNode).forEach(function(_2){var _3=dojo.attr(_2,"id");var _4=[0];dojo.query(".xfRadioValue",_2).attr("name",_1);var _5=new betterform.ui.Control({id:_3,value:dojo.attr(_2,"value")},_2);_5.startup();});this.inherited(arguments);this.setCurrentValue();},_onFocus:function $DA4t_(){this.inherited(arguments);this.handleOnFocus();},_onBlur:function $DA4u_(){this.inherited(arguments);this.handleOnBlur();},_setRadioGroupValue:function $DA4v_(){var _6=dojo.query(".dijitRadioChecked .dijitCheckBoxInput",this.domNode)[0];var _7;if(_6!=undefined){var _8=dojo.attr(_6,"id");_7=_8.substring(0,_8.length-6);}else{_7="";}fluxProcessor.dispatchEventType(this.xfControl.id,"DOMActivate",_7);if(this.incremental){this.setControlValue();}},getControlValue:function $DA4w_(){var _9=dojo.query(".dijitRadioChecked .dijitCheckBoxInput",this.domNode)[0];if(_9!=undefined){return dijit.byId(dojo.attr(_9,"id")).getControlValue();}else{return "";}},_handleSetControlValue:function $DA4x_(_a){var _b=dojo.query(".xfRadioValue",this.domNode);for(i=0;i<_b.length;i++){var _c=dojo.attr(_b[i],"widgetId");var _d=dijit.byId(_c);var _e=_d.getControlValue();if(_e==_a){_d._setCheckedAttr(true);}else{_d._setCheckedAttr(false);}}},applyState:function $DA4y_(){var _f=dojo.query(".xfRadioValue",this.domNode);for(i=0;i<_f.length;i++){var _10=dojo.byId(dojo.attr(_f[i],"widgetId"));if(this.xfControl.isReadonly()&&_10!=undefined){dojo.attr(_10,"disabled","disabled");}else{if(_10!=undefined){_10.removeAttribute("disabled");}}}}});}