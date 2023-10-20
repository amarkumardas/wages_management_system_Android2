package amar.das.acbook.pdfgenerator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import amar.das.acbook.activity.PdfViewerOperationActivity;
import amar.das.acbook.utility.MyUtility;

public class MakePdf{
    private PdfDocument myPdfDocument;
    private Paint myPaint;
    private PdfDocument.PageInfo myPageInfo;
    private Canvas canvas;
    private PdfDocument.Page myPage;

    private static final byte defaultSideGab =4;//gab between data and page
    public static int defaultPageWidth=350,defaultPageHeight=600;
    private static final byte gabBetweenComponents=3;//gabBetweenComponents ensure gab between components
    public  float currentHeightOfDataOfPage;//this variable cant be static otherwise produce wrong result and better not to initialize .IT KEEP TRACK TILL HOW MUCH height data is written SO THAT WE CAN MOVE TO NEXT PAGE IF PAGE IS FULL.it is updated when all content is written
    public MakePdf(){
       myPdfDocument= new PdfDocument();
        myPaint= new Paint();
    }
    public boolean makeTopHeaderOrganizationDetails(String headerOrgName, String extraString, String contact, String whatsappNumber, String email, boolean forAttachTrue){
        try {//automatically adjustable
//            top=(int)currentHeightOfDataOfPage;//updating top for use of next component
//            bottom=25;//updating bottom for use of next component
//            float right = myPageInfo.getPageWidth() - sideGab; // Define the right margin of the rectangle.
//            myPaint.setColor(Color.rgb(53, 77, 203));//blue
//            canvas.drawRect(sideGab, top, right, bottom, myPaint); // Draw the rectangle on the canvas.
//            below is improved version
            if(headerOrgName==null || headerOrgName.isEmpty()){
                headerOrgName="";
            }
            if(extraString==null || extraString.isEmpty()){
                extraString="";
            }
            if(contact==null || contact.isEmpty()){
                contact="";
            }
            if(whatsappNumber==null || whatsappNumber.isEmpty()){
                whatsappNumber="";
            }
            if(email==null || email.isEmpty()){
                email="";
            }
            attachOrDetachToComponentsAndUpdateCurrentHeightOfDataOfPage(forAttachTrue);
            byte heightOfRectangle=25;
            float top=currentHeightOfDataOfPage;//updating top for use of next component
            currentHeightOfDataOfPage=currentHeightOfDataOfPage+heightOfRectangle;//heightOfRectangle is height of rectangle so updating

            if(checkToAddNewPageAndUpdateCurrentHeightOfDataOfPage(currentHeightOfDataOfPage)) {//before drawing rectangle checking page height
                top = currentHeightOfDataOfPage;//since currentHeightOfDataOfPage is updated
                currentHeightOfDataOfPage = top + heightOfRectangle;//updating currentHeightOfDataOfPage  is important
            }
            myPaint.setColor(Color.rgb(53, 77, 203));//blue
            canvas.drawRect(defaultSideGab, top, myPageInfo.getPageWidth() - defaultSideGab, currentHeightOfDataOfPage, myPaint); // Draw the rectangle on the canvas.

            myPaint.setTextAlign(Paint.Align.LEFT);//text will be in middle
            myPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            myPaint.setColor(Color.WHITE);

            myPaint.setTextSize(11.0f);
            canvas.drawText(headerOrgName,  8,  top+12 , myPaint);

            myPaint.setTextAlign(Paint.Align.RIGHT);
            myPaint.setTextSize(6.0f);
            myPaint.setColor(Color.BLACK);
            canvas.drawText(extraString,  myPageInfo.getPageWidth()-7,  top+12 , myPaint);

            myPaint.setColor(Color.WHITE);
            myPaint.setTextAlign(Paint.Align.LEFT);//text will be in middle
            canvas.drawText("Contact: " + contact + ", Whatsapp: " + whatsappNumber + ", Email: " + email, 8,  top+20 , myPaint);//top+20 to dynamically get change

            currentHeightOfDataOfPage=currentHeightOfDataOfPage+gabBetweenComponents;//Update the data height variable with the height of the rectangle that was just drawn.to use for next component
            return true;
        }catch (Exception ex){
            //System.out.println("makeTopHeader1 method error****************************");
            ex.printStackTrace();
            return false;
        }
    }
    public boolean makePersonImageDetails(String name, String id, String accountNo, String aadhaarNo, byte [] image, String invoiceNo ,String activePhoneNumber, boolean forAttachTrue){
        try {
            if(name==null || name.isEmpty()){
                name="";
            }
            if(id==null || id.isEmpty()){
                id="";
            }
            if(accountNo==null || accountNo.isEmpty()){
                accountNo="";
            }
            if(aadhaarNo==null || aadhaarNo.isEmpty()){
                aadhaarNo="";
            }
            if(invoiceNo==null || invoiceNo.isEmpty()){
                invoiceNo="";
            }
            if(activePhoneNumber==null || activePhoneNumber.isEmpty()){
                activePhoneNumber="";
            }

            attachOrDetachToComponentsAndUpdateCurrentHeightOfDataOfPage(forAttachTrue);
            myPaint.setTextAlign(Paint.Align.LEFT);
            myPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            myPaint.setStyle(Paint.Style.STROKE);//myPaint.setStrokeWidth(2); another properties

            myPaint.setColor(Color.BLACK);
            byte heightOfRectangle=64;
            float top = currentHeightOfDataOfPage;
            currentHeightOfDataOfPage=currentHeightOfDataOfPage+heightOfRectangle;//heightOfRectangle is height of rectangle it is from top+65

            if(checkToAddNewPageAndUpdateCurrentHeightOfDataOfPage(currentHeightOfDataOfPage)) {//before drawing rectangle checking page height
                top = currentHeightOfDataOfPage;//since currentHeightOfDataOfPage is updated
                currentHeightOfDataOfPage = top + heightOfRectangle;//updating currentHeightOfDataOfPage  is important
            }

            canvas.drawRect(defaultSideGab, top, myPageInfo.getPageWidth() - defaultSideGab, currentHeightOfDataOfPage, myPaint); // Draw the rectangle on the canvas.

            myPaint.setColor(Color.BLACK);
            myPaint.setStrokeWidth(0);
            myPaint.setStyle(Paint.Style.FILL);

            myPaint.setTextSize(7.0f);
            canvas.drawText("NAME: " + name,80,  top+11 , myPaint);

            //to add rectangle color
           // myPaint.setColor(Color.YELLOW);
//            myPaint.setColor(Color.rgb(135, 137, 138));//grey
//            canvas.drawRect(80,top+21,  80+30  , top+21+5 , myPaint);//5 is height of color rectangle
//            myPaint.setColor(Color.BLACK);

            canvas.drawText("ID: " + id,80,  top+26 , myPaint);
            canvas.drawText("A/C: " + accountNo+", AADHAAR: "+aadhaarNo+", PHONE: "+activePhoneNumber,80,  top+41 , myPaint);

            myPaint.setTextSize(6.0f);
            canvas.drawText( "CREATED ON: "+ MyUtility.get12hrCurrentTimeAndDate(),80,  top+56 , myPaint);

            myPaint.setTextAlign(Paint.Align.RIGHT);
            myPaint.setTextSize(8.0f);
            canvas.drawText("INVOICE No. "+invoiceNo,  myPageInfo.getPageWidth()-7,  top+56 , myPaint);

            if(image !=null) {//if image is not null then only draw image
                myPaint.setTextAlign(Paint.Align.LEFT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 65, 62, false);//image size
                canvas.drawBitmap(scaledBitmap, 5, top + 1, myPaint);
            }

            currentHeightOfDataOfPage= currentHeightOfDataOfPage+gabBetweenComponents;// Update the data height variable with the height of the rectangle that was just drawn.

            return true;
        }catch (Exception ex){
           // System.out.println("makeImageDetails2 method error****************************");
            ex.printStackTrace();
            return false;
        }
    }
    public boolean makeTable(String[] headers, String[][] data, float[] columnWidths,float rowHeight,boolean forAttachTrue) {
        try {
            attachOrDetachToComponentsAndUpdateCurrentHeightOfDataOfPage(forAttachTrue);
            myPaint.setTextAlign(Paint.Align.CENTER);
            myPaint.setStyle(Paint.Style.FILL);
            myPaint.setColor(Color.BLACK);
            myPaint.setTextSize(7f);
            final byte gabBetweenRows=8;
            byte numColumns = (byte) headers.length;
            int numRows = data.length;

            if(numColumns != columnWidths.length){
                Log.d(this.getClass().getSimpleName(),"Check column and columnWidth length should be same in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
             }
            final float startX= defaultSideGab;
            final float startY= currentHeightOfDataOfPage ;//it is top value to start write

            float x = startX;//changeable variable x and y
            float y = startY;

            float availableWidth = defaultPageWidth - ((numColumns + 1) * myPaint.getStrokeWidth())-(defaultSideGab + defaultSideGab);/*sideGab  is the number of left and right side gab 4 from left and 4 from right.I can show an example of how availableWidth is calculated with some example values. Let's assume we have a table with 3 columns and a total width of 300 units. Let's also assume that the stroke width used for the column separators is 2 units.Using the formula from the code snippet, we can calculate the availableWidth as follows: availableWidth = tableWidth - (numColumns + 1) * myPaint.getStrokeWidth() = 300 - (3 + 1) * 2 = 300 - 8 = 292.Therefore, the availableWidth in this example is 292 units. This means that each column's width will need to be adjusted to fit within this available width, accounting for the space taken up by the column separators.*/
            float[] adjustedWidthsOfEachColumn = adjustColumnWidths(columnWidths, availableWidth);//width of each column

            // draw headers
            myPaint.setColor(Color.rgb(53, 77, 203));//BLUE

            if(checkToAddNewPageAndUpdateCurrentHeightOfDataOfPage((y+rowHeight))){//before drawing rectangle checking page height
                y = currentHeightOfDataOfPage;//updating y
            }

            canvas.drawRect(defaultSideGab,y,myPageInfo.getPageWidth() - defaultSideGab,y+rowHeight,myPaint); //headerHeight=rowHeight; is equal to header height

            myPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            for (int i = 0; i < numColumns; i++) {
                myPaint.setColor(Color.WHITE);
                canvas.drawText(headers[i], x + adjustedWidthsOfEachColumn[i] / 2, y + rowHeight / 2+2, myPaint);//data

                myPaint.setColor(Color.rgb(214, 218, 223));//light grey
                canvas.drawLine(x,y,x,y+rowHeight,myPaint);//vertical line
                myPaint.setColor(Color.BLACK);
                x += adjustedWidthsOfEachColumn[i] + myPaint.getStrokeWidth();//updating x to write
            }
             y +=rowHeight+gabBetweenRows;//updating y  gabBetweenRows is the value of distance to print rows FROM HEADERS

            myPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
            for (int row = 0; row < numRows; row++) {  // draw data rows

                x=startX;//startX is not changeable x is changeable.for new row always setting x value to initial value because x value will be same for all row initially
                float maxHeightColumnOfRow = maxHeightColumnOfRow(data[row], adjustedWidthsOfEachColumn,myPaint);//for each row max height

               if(checkToAddNewPageAndUpdateCurrentHeightOfDataOfPage((y+maxHeightColumnOfRow))) {//whenever new row checking for page height
                   y = currentHeightOfDataOfPage + 10;//10 because while draw line y-10 so plus here
               }

                for (int j=0;j<data[row].length;j++) {//single column data of row
                    float changeableY=y;//changeableY is for going to next line to print multiple line so whenever new row initializing
                    myPaint.setColor(Color.rgb(135, 137, 138));//grey
                    canvas.drawLine(x,y-10 ,x,y+maxHeightColumnOfRow-2,myPaint);//vertical line here x is acting as startColumn position to write text.y-10 since only y is low so taking from up -10
                    myPaint.setColor(Color.BLACK);

                String[] totalLinesOfStringArray = breakTextIntoLines(data[row][j], myPaint, adjustedWidthsOfEachColumn[j]);//convert text: amar kumar das to [amar ku],[mar das]

                    boolean incrementForSecondTime=false;
                    for (String columnLines : totalLinesOfStringArray) {//iterate column lines

                        if (incrementForSecondTime){
                            changeableY += calculateLineHeight(new String[]{"1LINEHEIGHT"}, myPaint);//to get one line height
                        }else{
                            incrementForSecondTime=true;//if there is multiple lines then initially if statement will execute and second always else will execute
                        }
                        canvas.drawText(columnLines, x + adjustedWidthsOfEachColumn[j] / 2, changeableY  , myPaint);
                        myPaint.setColor(Color.BLACK);
                }
                x= x+adjustedWidthsOfEachColumn[j] + myPaint.getStrokeWidth();//updating x it is changing according to column width
             }
                myPaint.setColor(Color.rgb(135, 137, 138));//grey
                canvas.drawLine(startX,y+maxHeightColumnOfRow-2,defaultPageWidth- defaultSideGab,y+maxHeightColumnOfRow-2,myPaint);//horizontal line
                canvas.drawLine(defaultPageWidth- defaultSideGab,y-10 ,defaultPageWidth- defaultSideGab,y+maxHeightColumnOfRow-2,myPaint);//last vertical line placing here is good
                myPaint.setColor(Color.BLACK);

                currentHeightOfDataOfPage=y+maxHeightColumnOfRow-2;//updating to keep track of page how much data row filled.it should be before y gets updated to use next component
                y += maxHeightColumnOfRow+gabBetweenRows;//updating y. gabBetweenRows is gab between row and should be fixed
              }
            currentHeightOfDataOfPage= currentHeightOfDataOfPage+gabBetweenComponents;//gabBetweenComponents so that next component would be drawn according to value of gabBetweenComponents

            return true;
        } catch (Exception ex) {
           // System.out.println("makeTable method error***************************");
            ex.printStackTrace();
            return false;
        }
    }

    public boolean singleCustomRow(String[] stringArray, float[] columnWidths,int column1TextColorDefaultValue0,int column2TextColorDefaultValue0,int column3TextColorDefaultValue0,int column4TextColorDefaultValue0,boolean forAttachTrue,byte leftSideGabDefault0,byte rightSideGabDefault0){
        try {
            attachOrDetachToComponentsAndUpdateCurrentHeightOfDataOfPage(forAttachTrue);
            myPaint.setTextAlign(Paint.Align.CENTER);
            myPaint.setStyle(Paint.Style.FILL);
            myPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            myPaint.setColor(Color.BLACK);
            myPaint.setTextSize(7f);

            byte extraSpaceInColumn=6;
            byte numColumns = (byte)columnWidths.length;

            float x;
            if(leftSideGabDefault0 == 0){
               x=leftSideGabDefault0=defaultSideGab;//x is changeable.for new row always setting x value to initial value because x value will be same for all row initially
            }else{
                x=leftSideGabDefault0;//x is changeable.for new row always setting x value to initial value because x value will be same for all row initially
            }
            if (rightSideGabDefault0 == 0){
                rightSideGabDefault0=defaultSideGab;
            }

            if(numColumns != stringArray.length){
                Log.d(this.getClass().getSimpleName(),"Check stringArray and columnWidth length should be same in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            }

            float availableWidth = defaultPageWidth - ((numColumns + 1) * myPaint.getStrokeWidth())-(leftSideGabDefault0 + rightSideGabDefault0);/* right and left sideGab is the number of left and right side gab 4 from left and 4 from right.I can show an example of how availableWidth is calculated with some example values. Let's assume we have a table with 3 columns and a total width of 300 units. Let's also assume that the stroke width used for the column separators is 2 units.Using the formula from the code snippet, we can calculate the availableWidth as follows: availableWidth = tableWidth - (numColumns + 1) * myPaint.getStrokeWidth() = 300 - (3 + 1) * 2 = 300 - 8 = 292.Therefore, the availableWidth in this example is 292 units. This means that each column's width will need to be adjusted to fit within this available width, accounting for the space taken up by the column separators.*/
            float[] adjustedWidthsOfEachColumn = adjustColumnWidths(columnWidths, availableWidth);//width of each column
            float maxHeightColumnOfRow = maxHeightColumnOfRow(stringArray, adjustedWidthsOfEachColumn,myPaint);//for each row max height

            checkToAddNewPageAndUpdateCurrentHeightOfDataOfPage((currentHeightOfDataOfPage+maxHeightColumnOfRow+extraSpaceInColumn));//whenever new row checking for page height and updating if greater then defaultPageHeight

            canvas.drawLine(leftSideGabDefault0,currentHeightOfDataOfPage,defaultPageWidth- rightSideGabDefault0,currentHeightOfDataOfPage ,myPaint);//horizontal line
            for (int column=0;column<stringArray.length;column++){//iterate single column stringArray of row

                    float changeableY=currentHeightOfDataOfPage ;//changeableY is for going to next line to print multiple line so whenever new row initializing
                    myPaint.setColor(Color.BLACK);
                    canvas.drawLine(x,currentHeightOfDataOfPage ,x,currentHeightOfDataOfPage+maxHeightColumnOfRow+extraSpaceInColumn,myPaint);//vertical line here x is acting as startColumn position to write text.y-10 since only y is low so taking from up -10

                    String[] totalLinesOfStringArray = breakTextIntoLines(stringArray[column], myPaint, adjustedWidthsOfEachColumn[column]);//convert text: amar kumar das to [amar ku],[mar das]

                  switch(column){//COLUMN COLOR
                      case 0:{if(column1TextColorDefaultValue0 != 0) myPaint.setColor(column1TextColorDefaultValue0);
                              else myPaint.setColor(Color.BLACK); break;}
                      case 1:{if(column2TextColorDefaultValue0 != 0) myPaint.setColor(column2TextColorDefaultValue0);
                              else myPaint.setColor(Color.BLACK); break;}
                      case 2:{if(column3TextColorDefaultValue0 != 0) myPaint.setColor(column3TextColorDefaultValue0);
                              else myPaint.setColor(Color.BLACK); break;}
                      case 3:{if(column4TextColorDefaultValue0 != 0) myPaint.setColor(column4TextColorDefaultValue0);
                              else myPaint.setColor(Color.BLACK); break;}
                      default:myPaint.setColor(Color.BLACK);
                  }
                  boolean incrementForSecondTime=false;
                    for (String columnLines : totalLinesOfStringArray) {//iterate column lines

                        if (incrementForSecondTime){
                            changeableY += calculateLineHeight(new String[]{"1LINEHEIGHT"}, myPaint);//to get one line height
                        }else{
                            incrementForSecondTime=true;//if there is multiple lines then initially if statement will execute and second always else will execute
                        }
                        canvas.drawText(columnLines, x + adjustedWidthsOfEachColumn[column]/2, changeableY+9, myPaint);//9 value to display text in middle
                    }
                    x= x+adjustedWidthsOfEachColumn[column] + myPaint.getStrokeWidth();//updating x it is changing according to column width
                }
                myPaint.setColor(Color.BLACK);
                canvas.drawLine(leftSideGabDefault0,currentHeightOfDataOfPage+maxHeightColumnOfRow+extraSpaceInColumn,defaultPageWidth- rightSideGabDefault0,currentHeightOfDataOfPage+maxHeightColumnOfRow+extraSpaceInColumn,myPaint);//last horizontal line
                canvas.drawLine(defaultPageWidth- rightSideGabDefault0,currentHeightOfDataOfPage ,defaultPageWidth- rightSideGabDefault0,currentHeightOfDataOfPage+maxHeightColumnOfRow+extraSpaceInColumn,myPaint);//last vertical line placing here is good

            currentHeightOfDataOfPage=currentHeightOfDataOfPage+maxHeightColumnOfRow+extraSpaceInColumn+gabBetweenComponents;//gabBetweenComponents so that next component would be drawn according to value of gabBetweenComponents

            return true;
        } catch (Exception ex) {
           // System.out.println("singleCustomRow method error***************************");
            ex.printStackTrace();
            return false;
        }
    }
    public boolean writeSentenceWithoutLines(String[] stringArray, float[] columnWidths ,boolean forAttachTrue,byte leftSideGabDefault0,byte rightSideGabDefault0){
        try {
            attachOrDetachToComponentsAndUpdateCurrentHeightOfDataOfPage(forAttachTrue);
            myPaint.setTextAlign(Paint.Align.CENTER);
            myPaint.setStyle(Paint.Style.FILL);
            myPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL));
            myPaint.setColor(Color.BLACK);
            myPaint.setTextSize(6f);

            byte numColumns = (byte)columnWidths.length;

            float x;
            if(leftSideGabDefault0 == 0){
                x=leftSideGabDefault0=defaultSideGab;//x is changeable.for new row always setting x value to initial value because x value will be same for all row initially
            }else{
                x=leftSideGabDefault0;//x is changeable.for new row always setting x value to initial value because x value will be same for all row initially
            }
            if (rightSideGabDefault0 == 0){
                rightSideGabDefault0=defaultSideGab;
            }

            if(numColumns != stringArray.length){
                Log.d(this.getClass().getSimpleName(),"Check stringArray and columnWidth length should be same. in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            }

            float availableWidth = defaultPageWidth - ((numColumns + 1) * myPaint.getStrokeWidth())-(leftSideGabDefault0 + rightSideGabDefault0);/* right and left sideGab is the number of left and right side gab 4 from left and 4 from right.I can show an example of how availableWidth is calculated with some example values. Let's assume we have a table with 3 columns and a total width of 300 units. Let's also assume that the stroke width used for the column separators is 2 units.Using the formula from the code snippet, we can calculate the availableWidth as follows: availableWidth = tableWidth - (numColumns + 1) * myPaint.getStrokeWidth() = 300 - (3 + 1) * 2 = 300 - 8 = 292.Therefore, the availableWidth in this example is 292 units. This means that each column's width will need to be adjusted to fit within this available width, accounting for the space taken up by the column separators.*/
            float[] adjustedWidthsOfEachColumn = adjustColumnWidths(columnWidths, availableWidth);//width of each column
            float maxHeightColumnOfRow = maxHeightColumnOfRow(stringArray, adjustedWidthsOfEachColumn,myPaint);//for each row max height

            checkToAddNewPageAndUpdateCurrentHeightOfDataOfPage((currentHeightOfDataOfPage+maxHeightColumnOfRow));//whenever new row checking for page height and updating if greater then defaultPageHeight

             for (int column=0;column<stringArray.length;column++){//iterate single column stringArray of row

                float changeableY=currentHeightOfDataOfPage ;//changeableY is for going to next line to print multiple line so whenever new row initializing
                myPaint.setColor(Color.BLACK);

                String[] totalLinesOfStringArray = breakTextIntoLines(stringArray[column], myPaint, adjustedWidthsOfEachColumn[column]);//convert text: amar kumar das to [amar ku],[mar das]

                boolean incrementForSecondTime=false;
                for (String columnLines : totalLinesOfStringArray) {//iterate column lines

                    if (incrementForSecondTime){
                        changeableY += calculateLineHeight(new String[]{"1LINEHEIGHT"}, myPaint);//to get one line height
                    }else{
                        incrementForSecondTime=true;//if there is multiple lines then initially if statement will execute and second always else will execute
                    }
                    canvas.drawText(columnLines, x + adjustedWidthsOfEachColumn[column]/2, changeableY+5, myPaint);//5 value to display text below
                }
                x= x+adjustedWidthsOfEachColumn[column] + myPaint.getStrokeWidth();//updating x it is changing according to column width
            }
             currentHeightOfDataOfPage=currentHeightOfDataOfPage+maxHeightColumnOfRow+gabBetweenComponents;//gabBetweenComponents so that next component would be drawn according to value of gabBetweenComponents

            return true;
        } catch (Exception ex) {
          //  System.out.println("writeSentenceWithoutLines method error***************************");
            ex.printStackTrace();
            return false;
        }
    }
    public void attachOrDetachToComponentsAndUpdateCurrentHeightOfDataOfPage(boolean forAttachTrue){//it will attach or detach to components
        if(forAttachTrue && currentHeightOfDataOfPage!= 0f){//since we are subtracting so checking currentHeightOfDataOfPage is 0 or not
            currentHeightOfDataOfPage=currentHeightOfDataOfPage-gabBetweenComponents;//if attach then we have to minus gabBetweenComponents so that it will attach.because currentHeightOfDataOfPage has already added value of gabBetweenComponents
        }
    }
    public boolean checkToAddNewPageAndUpdateCurrentHeightOfDataOfPage(float currentHeightOfData){//here currentHeightOfDataOfPage static variable is updated
        if(currentHeightOfData > defaultPageHeight) {
            currentHeightOfDataOfPage=addNewPageReturnValueOfYToStartWrite();//if the table goes beyond the bottom of the page, start a new page
           return true;
        }
        return false;
    }
   public float addNewPageReturnValueOfYToStartWrite() {
       //canvas.finishPage(page);
       myPdfDocument.finishPage(myPage);//to add new page to existing pdf then first we have to finish the page

       myPageInfo = new PdfDocument.PageInfo.Builder(defaultPageWidth, defaultPageHeight, 1).create();

       // page = pdfDocument.startPage(pageInfo);
       myPage =  myPdfDocument.startPage(myPageInfo);

       //canvas = page.getCanvas();
       canvas =  myPage.getCanvas();
       return 0f;
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

         Suppose we have a table with three columns, and we want the first column to have a width of 100 pixels, the second column to have a width  of 150 pixels,
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
    private float maxHeightColumnOfRow(String[] data, float[] adjustedWidths, Paint myPaint) {
        float maxHeight = 0;
        for (int i = 0; i < data.length; i++) {//all column height getting of row
            String[] lines = breakTextIntoLines(data[i],  myPaint, adjustedWidths[i]);
            float lineHeight = calculateLineHeight(lines,  myPaint);
            if (lineHeight > maxHeight) {
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
           // System.out.println("count_______"+count);
            lines.add(text.substring(start, start + count));//adding break sentence
           // System.out.println("........"+text.substring(start, start + count));
            start += count;
        }
        return lines.toArray(new String[0]);//In this example, we create a new ArrayList called lines and add three strings to it using the add method. Then, we use the toArray method to convert the ArrayList to an array of strings. The argument new String[0] is passed to the toArray method to indicate the type of the resulting array. The size of the array is determined automatically based on the size of the ArrayList.

        /*Paint.breakText() is used to determine where to break a text string into multiple lines in order to fit it within a specified width. This method takes a few parameters:

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
        /*metrics.descent: This refers to the distance from the baseline to the bottom of the lowest descender in the font. In other words, it represents
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

    public  File createFileToSavePdfDocumentAndReturnFile(String externalFileDir, String fileName){//return null when exception
        try {//externalFileDir is passed as string because this class is not extended with AppCompatActivity
//            File folder = new File( externalFileDir + "/acBookPDF");   //https://stackoverflow.com/questions/65125446/cannot-resolve-method-getexternalfilesdir
//            if (!folder.exists()) {//of folder not exist then create folder
//                folder.mkdir();//File createNewFile() method returns true if new file is created and false if file already exists.
//             }

            if(MyUtility.isFolderExistIfNotExistCreateIt(externalFileDir, PdfViewerOperationActivity.pdfFolderName)) {
                File filePath = new File(externalFileDir + "/"+ PdfViewerOperationActivity.pdfFolderName+"/" + fileName + ".pdf");//path of pdf where it is saved in device
                myPdfDocument.writeTo(new FileOutputStream(filePath.getAbsolutePath()));//if FileOutputStream cannot find file then it will create automatically
               // return filePath.getAbsolutePath();//returning created file absolute path
                return filePath;
            }else return null;

        }catch (Exception e){
       // System.out.println("CREATED PDF NOT COPIED TO DEVICE PDF FILE********************************************");
            e.printStackTrace();
            return null;
        }
        /*What is getExternalFileDir() in android stdio
          getExternalFilesDir() is a method in Android Studio that returns the path to the directory on the primary external storage device where the application can place persistent files it owns. These files are private to the application and are removed when the application is uninstalled.
          The method is used to access the external storage of an Android device, which is an area of the device's file system that can be accessed by the user and other applications. The external storage is typically used for storing user data that needs to persist even after the application is closed or the device is restarted.
          The getExternalFilesDir() method requires the WRITE_EXTERNAL_STORAGE permission to be granted in the application's manifest file. This permission allows the application to write to the external storage.
          The path returned by getExternalFilesDir() is specific to the application's package name, so different applications will have different directories on the external storage. The method returns null if the external storage is not currently available, such as when the device is connected to a computer and mounted as a USB storage device, or when the external storage is emulated on the device.*/
    }
    public boolean createPage1(Integer pageWidth, Integer pageHeight, Integer pageNumber){
        try {
            if(pageWidth >= defaultPageWidth && pageHeight >= defaultPageHeight) {
                defaultPageWidth=pageWidth;//updating
                defaultPageHeight=pageHeight;
                myPageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
            }else{
               // System.out.println("pageWidth and pageHeight value should be greater than default value***********************************");
                myPageInfo = new PdfDocument.PageInfo.Builder(defaultPageWidth, defaultPageHeight, pageNumber).create();
            }
             myPage =  myPdfDocument.startPage(myPageInfo);
             canvas =  myPage.getCanvas();
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean createdPageFinish2(){
        try {
            myPdfDocument.finishPage(myPage);
        }catch (Exception ex){
           // System.out.println("after page finish call you cannot write error*******************");
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
         // System.out.println("document is closed but trying to write error************************");
          return false;
      }
        return true;
    }
}
