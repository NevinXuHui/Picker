package com.hzc.picker;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.PngImage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.file.ClosedFileSystemException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PdfItextUtil {
    private Document document;
    private PdfWriter writer;
    private static final String TAG = "PdfItextUtil";

    // savePath:保存pdf的路径
    public PdfItextUtil(String savePath) throws Exception {
        Log.d(TAG, "PdfItextUtil: debug1");
        //创建新的PDF文档：A4大小，左右上下边框均为0

       // document = new Document(PageSize.ROYAL_QUARTO,0,0,0,0);

        document = new Document(new Rectangle(1404, 1872));
        Log.d(TAG, "PdfItextUtil: debug2");
        //获取PDF书写器
        FileOutputStream fileOutputStream = new FileOutputStream(savePath);
        Log.d(TAG, "PdfItextUtil: debug2.5");
        writer =PdfWriter.getInstance(document, fileOutputStream);
        Log.d(TAG, "PdfItextUtil: debug3");

        //打开文档
        document.open();
        Log.d(TAG, "PdfItextUtil: debug4");

    }
//保存目录
public int storeOutline(@NonNull String savePath) {
    try {
        PdfContentByte cb = writer.getDirectContent();
        PdfOutline root = cb.getRootOutline();
        PdfOutline sectionOutline1 = null;
        PdfOutline sectionOutline2 = null;
        PdfAction action;//标识书签点击后的跳转动作，通过它设置跳转的页码
        Log.d(TAG, "onCreate: PdfOutline savePath" + savePath);

        String sTxtPath = savePath + "/目录";
        String str;
        int flag = 0;
        BufferedReader bufRead = new BufferedReader(new FileReader(sTxtPath));
        List<Map<String, Object>> outlines = new ArrayList<>();//存放解析的数据
        while ((str = bufRead.readLine()) != null) {
            String[] ss = null;
            ss = str.split(",");
            if (flag == 0) {

                if (ss[0].equals("")) {
                    flag = 1;
                    sectionOutline1 = new PdfOutline(root, PdfAction.gotoLocalPage(Integer.valueOf(ss[ss.length - 1]), new PdfDestination(PdfDestination.FIT), writer), ss[1]);
                } else {
                    sectionOutline1 = new PdfOutline(root, PdfAction.gotoLocalPage(Integer.valueOf(ss[ss.length - 1]), new PdfDestination(PdfDestination.FIT), writer), ss[0]);
                }

            } else if (flag == 1) {

                if (ss[0].equals("")) {
                    sectionOutline1 = new PdfOutline(root, PdfAction.gotoLocalPage(Integer.valueOf(ss[ss.length - 1]), new PdfDestination(PdfDestination.FIT), writer), ss[1]);
                } else {
                    sectionOutline2 = new PdfOutline(sectionOutline1, PdfAction.gotoLocalPage(Integer.valueOf(ss[ss.length - 1]), new PdfDestination(PdfDestination.FIT), writer), ss[0]);
                }
            }


        }
    }catch (UnsupportedEncodingException | FileNotFoundException  e){
        e.printStackTrace();
        return -1;
    } catch (IOException e) {
        e.printStackTrace();
        return -1;
    }

    return 0;
//        PdfOutline oline1 = new PdfOutline(root,
//                PdfAction.gotoLocalPage(1, new PdfDestination(PdfDestination.FIT), writer), "目录");
//        PdfOutline oline2 = new PdfOutline(oline1,
//                PdfAction.gotoLocalPage(2, new PdfDestination(PdfDestination.FIT), writer), "目录1");
//        PdfOutline oline3 = new PdfOutline(oline2,
//                PdfAction.gotoLocalPage(3, new PdfDestination(PdfDestination.FIT), writer), "目录2");
//        PdfOutline oline4 = new PdfOutline(oline3,
//                PdfAction.gotoLocalPage(4, new PdfDestination(PdfDestination.FIT), writer), "目录3");

    }

    public void close(){
        if (document.isOpen()) {
            document.close();
        }
    }

    // 添加图片到pdf中，这张图片在pdf中居中显示
    // imgPath:图片的路径，我使用的是sdcard中图片
    // imgWidth：图片在pdf中所占的宽
    // imgHeight：图片在pdf中所占的高
    public int addImageToPdfCenterH(@NonNull String imgPath, float imgWidth, float imgHeight) {
        try{
            Image img = Image.getInstance(imgPath);
            img.setAlignment(Element.ALIGN_CENTER);
            // img.scaleToFit(imgWidth,imgHeight);
            //添加到PDF文档
            document.add(img);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return -1;
        } catch (BadElementException e) {
            e.printStackTrace();
            return -1;
        } catch (DocumentException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        //获取图片


        return 0;
    }

    public PdfItextUtil addPngToPdf(InputStream inputStream) throws DocumentException, IOException {
        Image img = PngImage.getImage(inputStream);
        img.setAlignment(Element.ALIGN_CENTER);
        //添加到PDF文档
        document.add(img);
        return this;
    }

    // 添加文本到pdf中
    public PdfItextUtil addTextToPdf(String content) throws DocumentException {
        Paragraph elements = new Paragraph(content, setChineseFont());
        elements.setAlignment(Element.ALIGN_BASELINE);
//        elements.setIndentationLeft(55);  //设置距离左边的距离
        document.add(elements); // result为保存的字符串
        return this;
    }

    // 给pdf添加个标题，居中黑体
    public PdfItextUtil addTitleToPdf(String title){
        try {
            Paragraph elements = new Paragraph(title, setChineseTiltleFont(18));
            elements.setAlignment(Element.ALIGN_CENTER);
            document.add(elements); // result为保存的字符串
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return this;
    }

    private Font setChineseFont() {
        return setChineseFont(12);
    }

    private Font setChineseFont(int size) {
        BaseFont bf;
        Font fontChinese = null;
        try {
            // STSong-Light : Adobe的字体
            // UniGB-UCS2-H : pdf 字体
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            fontChinese = new Font(bf, size, Font.NORMAL);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fontChinese;
    }

    private Font setChineseTiltleFont(int size) {
        BaseFont bf;
        Font fontChinese = null;
        try {
            // STSong-Light : Adobe的字体
            // UniGB-UCS2-H : pdf 字体
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            fontChinese = new Font(bf, size, Font.BOLD);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fontChinese;
    }
}
