// ICallback.aidl
package dc.krupal;

/**
 * Callback of print service execution results
 */
interface ICallback {

	/**
	* Return execution result
	* @param isSuccess:	  True execution succeeded, false execution failed
	*/
	oneway void onRunResult(boolean isSuccess);
	
	/**
	*Return result (string data)
	* @param result:	As a result, the print length (in mm) since the printer was powered on
	*/
	oneway void onReturnString(String result);
	
	/**
	* Execution exception
	* Code: exception code
	* Msg: exception description
	*/
	oneway void  onRaiseException(int code, String msg);
}