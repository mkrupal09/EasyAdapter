// IWoyouService.aidl

package dc.krupal;

import dc.krupal.ICallback;
import android.graphics.Bitmap;
import easyadapter.dc.com.easyadapter.TransBean;

interface IWoyouService
{


	/**
	* Printer firmware upgrade (only for system component calls, developer calls are invalid)
	* @param buffer			
	* @param size
	* @param filename
	* @param iapInterface
	*/	
	void updateFirmware();
	
	/**
	* Printer firmware status
	* return:  0--unknown, A5--bootloader, C3--print
	*/
	int getFirmwareStatus();
	
	/**
	* Take the WoyouService service version
	*/
	String getServiceVersion();	
	
	/**
	 * Initialize the printer, reset the printer's logic, but not clear the buffer data, so
	 * Incomplete print jobs will continue after reset
	 * @param callback
	 * @return
	 */
	void printerInit(in ICallback callback);
			
	/**
	*The printer self-test, the printer will print a self-test page
	* @param callback
	*/
	void printerSelfChecking(in ICallback callback);
	
	/**
	* Get the printer board serial number
	*/		
	String getPrinterSerialNo();
	
	/**
	* Get the printer firmware version number
	*/
	String getPrinterVersion();	
	
	/**
	* Get the printer model
	*/		
	String getPrinterModal();
	
	/**
	* Get printhead print length
	*/
	void getPrintedLength(in ICallback callback);
		
	/**
	 * The printer feeds paper (forced line feed, after the end of the print content, the paper is n lines)
	 * @param n:	Number of lines
	 * @param callback  Result callback
	 * @return
	 */
	void lineWrap(int n, in ICallback callback);
				
	/**
	* Print using the original instructions
	* @param data	    instruction
	* @param callback  Result callback
	*/
	void sendRAWData(in byte[] data, in ICallback callback);
	
	/**
	* Set the alignment mode to have an effect on subsequent printing unless initialized
	* @param alignment:	Alignment 0--Left , 1--Centered, 2--Right
	* @param callback  结果回调
	*/
	void setAlignment(int alignment, in ICallback callback);

	/**
	*Set the print font to have an effect on the print afterwards, unless initialized
	*(Currently only one font "gh" is supported, gh is a monospaced Chinese font, and more font options will be provided later)
	* @param typeface:		Font name
	*/
	void setFontName(String typeface, in ICallback callback);
	
	/**
	* Set the font size, which has an effect on printing afterwards, unless initialized
	*Note: The font size is printed beyond the standard international directives.
	* Adjusting the font size will affect the character width, and the number of characters per line will also change.
	* Therefore, the layout formed by the monospaced font may be confusing.
	* @param fontsize:	font size
	*/
	void setFontSize(float fontsize, in ICallback callback);
	
	/**
	* Print text, the text width is full of one line, and it is automatically wrapped and typeset. If it is not full, it will not print unless it is forced to wrap.
	* @param text:	The text string to be printed
	*/
	void printText(String text, in ICallback callback);

	/**
	*Print the text of the specified font, the font setting is only valid for this time.
	* @param text:			To print text
	* @param typeface:		Font name (currently only supports "gh" font)
	* @param fontsize:		font size
	*/
	void printTextWithFont(String text, String typeface, float fontsize, in ICallback callback);

	/**
	* Print a row of the table, you can specify the column width, alignment
	* @param colsTextArr   Array of text strings for each column
	* @param colsWidthArr  Array of column widths (in English characters, each Chinese character occupies two English characters, each width is greater than 0)
	* @param colsAlign	       Alignment of columns (0 left, 1 center, 2 right)
	* Remarks: The array length of the three parameters should be the same. If the width of colsText[i] is greater than colsWidth[i], the text is wrapped.
	*/
	void printColumnsText(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, in ICallback callback);

	
	/**
	* Print picture
	* @param bitmap: 	Image bitmap object (maximum width 384 pixels, more than unprintable and callback callback exception function)
	*/
	void printBitmap(in Bitmap bitmap, in ICallback callback);
	
	/**
	* Print one-dimensional barcode
	* @param data: 		Bar code data
	* @param symbology: 	Barcode type
	*    0 -- UPC-A
	*    1 -- UPC-E
	*    2 -- JAN13(EAN13)
	*    3 -- JAN8(EAN8)
	*    4 -- CODE39
	*    5 -- ITF
	*    6 -- CODABAR
	*    7 -- CODE93
	*    8 -- CODE128
	* @param height: 		Bar code height, Value 1 to 255, Default 162
	* @param width: 		Bar code width, Value 2 to 6, Default 2
	* @param textposition:	Text position 0--Do not print text, 1--Text above the barcode, 2--Text below the barcode, 3--Both the top and bottom of the barcode are printed
	*/
	void printBarCode(String data, int symbology, int height, int width, int textposition,  in ICallback callback);
		
	/**
	* Print 2D barcode
	* @param data:			QR code data
	* @param modulesize:	Two-dimensional code block size (unit: point, value 1 to 16)
	* @param errorlevel:	QR code error correction level (0 to 3),
	*                0 -- Error correction level L (7%),
	*                1 -- Error correction level M (15%),
	*                2 -- Error correction level Q (25%),
	*                3 -- Error correction level H (30%)
	*/
	void printQRCode(String data, int modulesize, int errorlevel, in ICallback callback);
	
	/**
	*Print text, the text width is full of one line, and it is automatically wrapped and typeset. If it is not full, it will not print unless it is forced to wrap.
	* The text is output as it is in the width of the vector text, that is, each character is not equal in width
	* @param text:	The text string to be printed
	* 
	*/
	void printOriginalText(String text, in ICallback callback);	
	
	/**
	* Lib package transaction printing dedicated interface
	* transbean		Print task list
	* Ver 1.8.0Increase
	*/
	void commitPrint(in TransBean[] transbean, in ICallback callback);
	
	/**
	* Print buffer content
	*/
	void commitPrinterBuffer();
	
	/**
	* Enter buffer mode, all print calls will be cached, print after calling promisePrinterBuffe()
	* 
	* @param clean: Whether to clear the buffer contents
	* 
	*/
	void enterPrinterBuffer(in boolean clean);
	
	/**
	* Exit buffer mode
	* 
	* @param commit: Whether to print out the contents of the buffer
	* 
	*/
	void exitPrinterBuffer(in boolean commit);
	
}