function _classCallCheck(n,e){if(!(n instanceof e))throw new TypeError("Cannot call a class as a function")}var _this=this,_typeof="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(n){return typeof n}:function(n){return n&&"function"==typeof Symbol&&n.constructor===Symbol&&n!==Symbol.prototype?"symbol":typeof n},_createClass=function(){function n(n,e){for(var t=0;t<e.length;t++){var o=e[t];o.enumerable=o.enumerable||!1,o.configurable=!0,"value"in o&&(o.writable=!0),Object.defineProperty(n,o.key,o)}}return function(e,t,o){return t&&n(e.prototype,t),o&&n(e,o),e}}();!function(){var n=function(n,e,t){var o="",r=void 0;t&&(r=new Date,r.setTime(r.getTime()+24*t*60*60*1e3),o=["; expires=",r.toGMTString()].join(""));var i=[n,"=",e,o,"; path=/"].join("");return document.cookie=i,e},e=function(n){for(var e=n+"=",t=document.cookie.split(";"),o=void 0,r=0;r<t.length;r++){for(o=t[r];" "===o.charAt(0);)o=o.substring(1,o.length);if(0===o.indexOf(e))return o.substring(e.length,o.length)}return null},t=function(){for(var n=[],e="0123456789abcdef",t=0;t<36;t++)n.push(e.substr(Math.floor(16*Math.random()),1));return n[14]="4",n[19]=e.substr(3&n[19]|8,1),n[8]=n[13]=n[18]=n[23]="-",n.join("")},o=function(o){return e(o)||n(o,t(),7)},r=function(n){if(!n.clientVersion)throw console.log("Error. The clientVersion field cannot be null in the log entry"),new Error("The clientVersion field cannot be null in the log entry");if(!n.source)throw console.log("Error. The source field cannot be null in the log entry"),new Error("The source field cannot be null in the log entry");if(!n.componentId)throw console.log("Error. The componentId field cannot be null in the log entry"),new Error("The componentId field cannot be null in the log entry");if(!n.name)throw console.log("Error. The name field cannot be null in the log entry"),new Error("The guid field cannot be null in the log entry");var e=n.logVersion||"2",t=n.clientSystemTime||(new Date).getTime();return Object.assign({},n,{logVersion:e,clientSystemTime:t})},i=function(n){var e=r(n),t="https://hlg.tokbox.com/prod/logging/ClientEvent",o=new XMLHttpRequest;o.open("POST",t,!0),o.setRequestHeader("Content-type","application/json"),o.send(JSON.stringify(e))},l=function(){function n(e){_classCallCheck(this,n),this.analyticsData=e,this.analyticsData.guid=o(e.name)}return _createClass(n,[{key:"addSessionInfo",value:function(n){if(!n.sessionId)throw console.log("Error. The sessionId field cannot be null in the log entry"),new Error("The sessionId field cannot be null in the log entry");if(this.analyticsData.sessionId=n.sessionId,!n.connectionId)throw console.log("Error. The connectionId field cannot be null in the log entry"),new Error("The connectionId field cannot be null in the log entry");if(this.analyticsData.connectionId=n.connectionId,0===n.partnerId)throw console.log("Error. The partnerId field cannot be null in the log entry"),new Error("The partnerId field cannot be null in the log entry");this.analyticsData.partnerId=n.partnerId}},{key:"logEvent",value:function(n){this.analyticsData.action=n.action,this.analyticsData.variation=n.variation,this.analyticsData.clientSystemTime=(new Date).getTime(),i(this.analyticsData)}}]),n}();"object"===("undefined"==typeof exports?"undefined":_typeof(exports))?module.exports=l:"function"==typeof define&&define.amd?define(function(){return l}):_this.OTKAnalytics=l}(this);