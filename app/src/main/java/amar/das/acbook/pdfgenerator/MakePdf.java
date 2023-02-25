package amar.das.acbook.pdfgenerator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import amar.das.acbook.utility.ProjectUtility;

public class MakePdf{
    private PdfDocument myPdfDocument;
    private Paint myPaint;
    private PdfDocument.PageInfo myPageInfo;
    private Canvas canvas;
    private PdfDocument.Page mypage;
    public static Integer defaultPageWidth=290,defaultPageHeight=500;
    public static Integer keepTrackOfPageDataHeight=2;//IT KEEP TRACK TILL HOW MUCH height data is written SO THAT WE CAN MOVE TO NEXT PAGE IF PAGE IS FULL
    public MakePdf(){
       myPdfDocument= new PdfDocument();
        myPaint= new Paint();
    }
    public boolean makeTopHeader1(String headerOrgName, String contact, String whatsappNumber,String email){
        try {//automatically adjustable
            myPaint.setColor(Color.rgb(53, 77, 203));//blue
            keepTrackOfPageDataHeight =myPageInfo.getPageHeight() - Math.abs(25-myPageInfo.getPageHeight());//height end value
                                                                                                                  //make negative to positive number
            canvas.drawRect(4, myPageInfo.getPageHeight()-Math.abs(2-myPageInfo.getPageHeight()), myPageInfo.getPageWidth() - 4,keepTrackOfPageDataHeight , myPaint);

            myPaint.setTextAlign(Paint.Align.LEFT);//text will be in middle
            myPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            myPaint.setTextSize(11.0f);
            myPaint.setColor(Color.WHITE);
            canvas.drawText(headerOrgName,  8, myPageInfo.getPageHeight()-Math.abs(13-myPageInfo.getPageHeight()), myPaint);
            myPaint.setTextSize(6.0f);
            canvas.drawText("Contact: " + contact + ", Whatsapp: " + whatsappNumber + ", Email: " + email, 8, myPageInfo.getPageHeight()-Math.abs(21-myPageInfo.getPageHeight()), myPaint);
            return true;
        }catch (Exception ex){
            System.out.println("makeTopHeader1 method error****************************");
            ex.printStackTrace();
            return false;
        }
    }
    public boolean makeSubHeader2ImageDetails(String name, String id, String accountNo, String aadhaarNo, byte [] image, String invoiceNo ){
        try {//automatically adjustable
            myPaint.setTextAlign(Paint.Align.LEFT);
            myPaint.setStyle(Paint.Style.STROKE);//myPaint.setStrokeWidth(2); another propertiees
            myPaint.setColor(Color.BLACK);
            keepTrackOfPageDataHeight =myPageInfo.getPageHeight() - Math.abs(90-myPageInfo.getPageHeight());//top value is updated because all data will be store in this rectangla
                                                                          //28 is value of top
            canvas.drawRect(4, myPageInfo.getPageHeight()-Math.abs(28-myPageInfo.getPageHeight()), myPageInfo.getPageWidth() - 4,keepTrackOfPageDataHeight , myPaint);

            myPaint.setColor(Color.BLACK);
            myPaint.setStrokeWidth(0);
            myPaint.setStyle(Paint.Style.FILL);
            myPaint.setTextSize(7.0f);
            canvas.drawText("NAME: " + name,80, myPageInfo.getPageHeight()-Math.abs(39-myPageInfo.getPageHeight()), myPaint);

            //myPaint.setStrokeWidth(2); another propertiees
            myPaint.setColor(Color.YELLOW);
            canvas.drawRect(myPageInfo.getPageWidth() -Math.abs(80-myPageInfo.getPageWidth()),48, myPageInfo.getPageWidth() -Math.abs(150-myPageInfo.getPageWidth()) ,myPageInfo.getPageHeight() - Math.abs(55-myPageInfo.getPageHeight()) , myPaint);

            myPaint.setColor(Color.BLACK);
            canvas.drawText("ID: " + id,80, myPageInfo.getPageHeight()-Math.abs(54-myPageInfo.getPageHeight()), myPaint);
            canvas.drawText("A/C: " + accountNo+", AADHAAR: "+aadhaarNo,80, myPageInfo.getPageHeight()-Math.abs(69-myPageInfo.getPageHeight()), myPaint);
            myPaint.setTextSize(6.0f);
            canvas.drawText( "CREATED ON: "+ ProjectUtility.get12hrCurrentTimeAndDate(),80, myPageInfo.getPageHeight()-Math.abs(84-myPageInfo.getPageHeight()), myPaint);

            myPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("INVOICE No. "+invoiceNo,  myPageInfo.getPageWidth()-7, myPageInfo.getPageHeight()-Math.abs(84-myPageInfo.getPageHeight()), myPaint);

            myPaint.setTextAlign(Paint.Align.LEFT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,65,60,false);//image size
            canvas.drawBitmap(scaledBitmap,5,myPageInfo.getPageHeight()-Math.abs(29-myPageInfo.getPageHeight()),myPaint);
            return true;
        }catch (Exception ex){
            System.out.println("makeImageDetails2 method error****************************");
            ex.printStackTrace();
            return false;
        }
    }
    public boolean makeTable(String[] headers, String[][] data, float[] columnWidths,float rowHeight) {
        try {
            int numColumns = headers.length;
            int numRows = data.length;
            float pageWidth=defaultPageWidth;
            float pageHeight=defaultPageHeight;

            myPaint.setTextAlign(Paint.Align.CENTER);
            myPaint.setStyle(Paint.Style.FILL);

            //not changeable variable startX and startY
            float startX=4;
            float startY=keepTrackOfPageDataHeight+5;//90+5

            //changeable variable x and y
            float x = startX;
            float y = startY;
                                 //defaultPageWidth is equal to table width
            float availableWidth = defaultPageWidth - ((numColumns + 1) * myPaint.getStrokeWidth());/*I can show an example of how availableWidth is calculated with some example values. Let's assume we have a table with 3 columns and a total width of 300 units. Let's also assume that the stroke width used for the column separators is 2 units.Using the formula from the code snippet, we can calculate the availableWidth as follows: availableWidth = tableWidth - (numColumns + 1) * myPaint.getStrokeWidth() = 300 - (3 + 1) * 2 = 300 - 8 = 292.Therefore, the availableWidth in this example is 292 units. This means that each column's width will need to be adjusted to fit within this available width, accounting for the space taken up by the column separators.*/
            float[] adjustedWidthsOfEachColumn = adjustColumnWidths(columnWidths, availableWidth);

            // draw headers
            myPaint.setColor(Color.rgb(53, 77, 203));//blue
            canvas.drawRect(4,y,myPageInfo.getPageWidth() - 4,y+rowHeight,myPaint); //headerHeight=rowHeight; is equal to header height
            for (int i = 0; i < numColumns; i++) {
                myPaint.setColor(Color.WHITE);
                canvas.drawText(headers[i], x + adjustedWidthsOfEachColumn[i] / 2, y + rowHeight / 2+2, myPaint);//data

                 myPaint.setColor(Color.BLACK);
                 canvas.drawLine(x,y,x,y+rowHeight,myPaint);//verticle line

                x += adjustedWidthsOfEachColumn[i] + myPaint.getStrokeWidth();//updating x to write
            }

            y += rowHeight;//updating y

            // draw data rows
            for (int i = 0; i < numRows; i++) {
                myPaint.setTextAlign(Paint.Align.CENTER);
                x = startX;
                float maxHeighColumnOfRow = calculateMaxHeight(data[i], adjustedWidthsOfEachColumn, rowHeight,myPaint);//for each row max height
                System.out.println("--------------max height of first row: "+maxHeighColumnOfRow);
                rowHeight=maxHeighColumnOfRow;//updating row height to print in middle
                int j=0;
                for (String columnData : data[i]) {//column data of row
                    // canvas.drawLine(startX,y,x,y+maxHeighColumnOfRow,myPaint);//verticle line

                    String[] totalLinesOfStringArray = breakTextIntoLines(columnData, myPaint, adjustedWidthsOfEachColumn[j]);//convert text: amar kumar das to [amar ku],[mar das]
                    for (String s:totalLinesOfStringArray) {
                       // System.out.println("*****"+s);
                    }
                    float totalLineHeight = calculateLineHeight(totalLinesOfStringArray, myPaint);
                    System.out.println("***line height:"+totalLineHeight);

//                    if (totalLineHeight > rowHeight) {//this is only to update rowheight so that in middle we can print text
//                        System.out.println("-------executed is only to update rowheight");
//                        rowHeight = totalLineHeight;
//                        maxHeighColumnOfRow = Math.max(maxHeighColumnOfRow, rowHeight);//this will be same and not required i guess
//                    }

                    if(adjustedWidthsOfEachColumn.length-1==j){//when column is last ie.description the start from left
                        myPaint.setTextAlign(Paint.Align.LEFT);
                    }

                     Boolean allowYtoIncrement=false;
                    for (String columnlines : totalLinesOfStringArray) {
                        System.out.println("*****"+columnlines);
                        if (y + maxHeighColumnOfRow > pageHeight) {
                            // if the table goes beyond the bottom of the page, start a new page

                            System.out.println("new page___________________________________________");
                            //canvas.finishPage(page);
                            myPdfDocument.finishPage(mypage);

                            myPageInfo = new PdfDocument.PageInfo.Builder(defaultPageWidth, defaultPageHeight, 1).create();

                            // page = pdfDocument.startPage(pageInfo);
                            mypage =  myPdfDocument.startPage(myPageInfo);

                            //canvas = page.getCanvas();
                            canvas =  mypage.getCanvas();
                            y = startY;
                        }
                        System.out.println("check: x:"+x+" weidth:"+adjustedWidthsOfEachColumn[j]+" x+width:"+(x + adjustedWidthsOfEachColumn[j])+" y:"+y+ " pagewidth:"+pageWidth);

                        if (x + adjustedWidthsOfEachColumn[j] > pageWidth) {
                            System.out.println("----greater then page weight");
                            // if the columnData goes beyond the right edge of the page, wrap the text to the next columnlines
                            x = startX;

                            if (allowYtoIncrement) {
                                y += calculateLineHeight(new String[]{"single line height"}, myPaint);
                            }else{
                                allowYtoIncrement=true;
                            }
                        }
                        System.out.println("check: x:"+x+" weidth:"+adjustedWidthsOfEachColumn[j]+" x+width:"+(x + adjustedWidthsOfEachColumn[j])+" y:"+y+ " pagewidth:"+pageWidth);
                        System.out.println("*****printed:"+columnlines);
                        canvas.drawText(columnlines, x + adjustedWidthsOfEachColumn[j] / 2, y + rowHeight / 2, myPaint);
                        x += adjustedWidthsOfEachColumn[j] + myPaint.getStrokeWidth();
                    }
                    j++;
                }
                y += maxHeighColumnOfRow;
              }

            return true;
        } catch (Exception ex) {
            System.out.println("makeTable method error***************************");
            ex.printStackTrace();
            return false;
        }
    }

    private float[] adjustColumnWidths(float[] columnWidths, float availableWidth) {
        float totalWidth = 0;
        for (float width : columnWidths) {//getting total width
            totalWidth += width;
        }
        float scaleFactor = availableWidth / totalWidth;
        float[] adjustedWidths = new float[columnWidths.length];
        for (int i = 0; i < columnWidths.length; i++) {
            adjustedWidths[i] = columnWidths[i] * scaleFactor;
        }
        return adjustedWidths;
        /*Explain by putting value in float scaleFactor = availableWidth / totalWidth;
         Here's an example to illustrate how the value of scaleFactor is calculated:

         Suppose we have a table with three columns, and we want the first column to have a width of 100 pixels, the second column to have a width of 150 pixels,
         and the third column to have a width of 200 pixels. We have a total width of 500 pixels available for the table.

         To calculate the value of scaleFactor, we first need to add up the desired width of all the columns:

         totalWidth = 100 + 150 + 200 = 450 Next, we divide the available width by the total width to get the scale factor:

         scaleFactor = 500 / 450 ≈ 1.11  //450*1.11=500
         This means that each column needs to be scaled up by a factor of 1.11 to fit within the available width of 500 pixels. To calculate the adjusted width
         for each column, we multiply the desired width of each column by the scale factor:

         adjustedWidths[0] = 100 * 1.11 ≈ 111 adjustedWidths[1] = 150 * 1.11 ≈ 167 adjustedWidths[2] = 200 * 1.11 ≈ 222

         So the adjusted widths for the columns would be approximately 111 pixels, 167 pixels, and 222 pixels, respectively, which should fit within the
         available width of 500 pixels.*/
    }
    private float calculateMaxHeight(String[] data, float[] adjustedWidths, float rowHeight, Paint myPaint) {
        float maxHeight = rowHeight;
        for (int i = 0; i < data.length; i++) {

            String[] lines = breakTextIntoLines(data[i],  myPaint, adjustedWidths[i]);
            float lineHeight = calculateLineHeight(lines,  myPaint);
            if (lineHeight > rowHeight) {
                // If the height of the current cell is greater than the current row height,
                // update the row height to match the height of the current cell.
                maxHeight = lineHeight;
            }
        }
        return maxHeight;
    }
    private String[] breakTextIntoLines(String text, Paint paint, float maxWidth) {// Helper method to break text into multiple lines to fit within the given width
        if (text == null || text.isEmpty()) {
            return new String[]{""};
        }
        List<String> lines = new ArrayList<>();
        int start = 0;
        int end = text.length();
        while (start < end) {
            int count = paint.breakText(text, start, end, true, maxWidth, null);
            lines.add(text.substring(start, start + count));//adding break sentence
            start += count;
        }
        return lines.toArray(new String[0]);//In this example, we create a new ArrayList called lines and add three strings to it using the add method. Then, we use the toArray method to convert the ArrayList to an array of strings. The argument new String[0] is passed to the toArray method to indicate the type of the resulting array. The size of the array is determined automatically based on the size of the ArrayList.

        /**Paint.breakText() is used to determine where to break a text string into multiple lines in order to fit it within a specified width. This method takes a few parameters:

         text: The text to measure.
         start: The index of the first character to measure.
         end: The index of the last character to measure.
         measureForwards:If true, the method measures the text from the start of the string to the end. If false, it measures the text from the end of the string to the start.
         maxWidth: The maximum width of the text.

         The method returns the number of characters that fit within the specified width. This can be used to break the text into multiple lines, if necessary*/
    }

    // Helper method to calculate the height of a line of text
    private float calculateLineHeight(String[] lines, Paint paint) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        return (metrics.descent - metrics.ascent) * lines.length + metrics.leading * (lines.length - 1);
        /**metrics.descent: This refers to the distance from the baseline to the bottom of the lowest descender in the font. In other words, it represents
         * how far below the baseline the lowest point of a character can extend.

         metrics.ascent: This refers to the distance from the baseline to the top of the highest ascender in the font. In other words, it represents
         how far above the baseline the highest point of a character can extend.

         (metrics.descent - metrics.ascent): This calculates the total height of a single line of text, by subtracting the ascent from the descent.

         lines.length: This is the number of lines in the block of text.

         (metrics.descent - metrics.ascent) * lines.length: This calculates the total height of all the lines in the block of text, by multiplying the
         height of a single line by the number of lines.

         metrics.leading: This refers to the distance between the baselines of consecutive lines of text. It's basically the extra space between lines that
         makes text more readable.

         (lines.length - 1): This calculates the number of gaps between lines (i.e., the number of times the leading distance is needed) by subtracting
         1 from the number of lines.

         metrics.leading * (lines.length - 1): This calculates the total height of all the gaps between lines, by multiplying the leading distance by the
         number of gaps.

         (metrics.descent - metrics.ascent) * lines.length + metrics.leading * (lines.length - 1): This adds together the total height of all the lines and
         the total height of all the gaps to get the total height of the block of text.*/
    }

    public  String createFileToSavePdfDocumentAndReturnFilePath3(String externalFileDir,String fileName){
        try {
            File folder = new File( externalFileDir + "/acBookPDF");   //https://stackoverflow.com/questions/65125446/cannot-resolve-method-getexternalfilesdir
            if (!folder.exists()) {//of folder not exist then create folder
                folder.mkdir();//File createNewFile() method returns true if new file is created and false if file already exists.
                System.out.println("Creating acBookPDF folder to store PDF***********************************************");
            }
            File filees = new File( externalFileDir + "/acBookPDF/" + fileName + ".pdf");//path of pdf where it is saved in device
            myPdfDocument.writeTo(new FileOutputStream(filees.getAbsolutePath()));//if FileOutputStream cannot find file then it will create automatically
            return filees.getAbsolutePath();//returning created file absolute path

        }catch (Exception e){
        System.out.println("CREATED PDF NOT COPIED TO DEVICE PDF FILE********************************************");
            e.printStackTrace();
            return null;
        }
    }
    public boolean createNewPage1(Integer pageWidth,Integer pageHeight,Integer pageNumber){
        try {
            if(pageWidth >= defaultPageWidth && pageHeight >= defaultPageHeight) {
                defaultPageWidth=pageWidth;//updating
                defaultPageHeight=pageHeight;
                myPageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
            }else{
                System.out.println("pageWidth and pageHeight value should be greater than default value***********************************");
                myPageInfo = new PdfDocument.PageInfo.Builder(defaultPageWidth, defaultPageHeight, pageNumber).create();
            }
             mypage =  myPdfDocument.startPage(myPageInfo);
             canvas =  mypage.getCanvas();
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean createdPageFinish2(){
        try {
            myPdfDocument.finishPage(mypage);
        }catch (Exception ex){
            System.out.println("after page finish call you cannot write error*******************");
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean closeDocumentLastOperation4(){//Closes this document. This method should be called after you are done working with the document. After this call the document is considered closed and none of its methods should be called.
      try {
           myPdfDocument.close();
      }catch (Exception ex){
          ex.printStackTrace();
          System.out.println("document is closed but trying to write error************************");
          return false;
      }
        return true;
    }

    public PdfDocument getMyPdfDocument() {
        return myPdfDocument;
    }

    public Paint getMyPaint() {
        return myPaint;
    }

    public PdfDocument.PageInfo getMyPageInfo() {
        return myPageInfo;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public PdfDocument.Page getMypage() {
        return mypage;
    }

}
