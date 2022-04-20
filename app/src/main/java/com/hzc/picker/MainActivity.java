package com.hzc.picker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hzc.widget.picker.file.FilePicker;
import com.hzc.widget.picker.file.FilePickerUiParams;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvResult;
    private Button button;
    private PdfItextUtil pdfItextUtil;
    private ArrayList<String> mList = new ArrayList<>();
    private static final String TAG = "MainActivity";

    private static final int PDF_SAVE_START = 1;// 保存PDF文件的开始意图
    private static final int PDF_SAVE_RESULT = 2;// 保存PDF文件的结果开始意图
    private static final int PDF_SAVE_ERROR = 3;// 保存PDF文件的结果开始意图
    private static final int PDF_SAVE_PROGRESS = 4;// 保存PDF文件的结果开始意图

    private ProgressDialog myDialog; // 保存进度框

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case PDF_SAVE_START:
                    Log.d(TAG, "handleMessage: PDF_SAVE_START");
                    if (!myDialog.isShowing())
                        myDialog.show();
                    break;

                case PDF_SAVE_RESULT:
                    if (myDialog.isShowing())
                        myDialog.dismiss();
                    Log.d(TAG, "handleMessage: PDF_SAVE_RESULT");
                    Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case PDF_SAVE_ERROR:
                    if (myDialog.isShowing())
                        myDialog.dismiss();
                    Log.d(TAG, "handleMessage: PDF_SAVE_ERROR");
                    Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case PDF_SAVE_PROGRESS:
                    if (myDialog.isShowing())
                        myDialog.setMessage(String.format("当前保存进度：%d%%",msg.arg1));
                    Log.d(TAG, "handleMessage: PDF_SAVE_PROGRESS");
//                    Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_SHORT)
//                            .show();
                    break;
            }
            return false;
        }
    });

    /**
     * 初始化识别进度框
     */
    private void initProgress() {
        myDialog = new ProgressDialog(this, ProgressDialog.STYLE_HORIZONTAL);
        myDialog.setIndeterminateDrawable(getResources().getDrawable(
                R.drawable.progress_ocr));

        myDialog.setMax(100);
        myDialog.setProgress(70);
        myDialog.setMessage("当前保存进度：0%");
        myDialog.setCanceledOnTouchOutside(false);
        myDialog.setCancelable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String data = intent.getStringExtra("extra_data");
        Log.d(TAG, "onResume: "+data);

//        if(data != null){
//            toPDF(data);
//        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvResult = findViewById(R.id.tv_result);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initProgress();
                checkNeedPermissions();
                filePickerExe();
            }
        });

        initProgress();
        checkNeedPermissions();

        Intent intent = getIntent();
        String data = intent.getStringExtra("extra_data");
        if(data != null){
            Log.d(TAG, "onCreate: 3212321321");
            toPDF(data);
        }
        else{
            filePickerExe();
        }



        Log.d(TAG, "onCreate: "+data);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        Intent intent = getIntent();
//        String data2 = intent.getStringExtra("extra_data");
//        if(data2 != null){
//            toPDF(data2);
//            return;
//        }

        FilePicker.onActivityResult(this, requestCode, resultCode, data);
    }




    private void  filePickerExe(){
        FilePicker.build(MainActivity.this, 1)
                .setPickFileType(FilePickerUiParams.PickType.FILE_OR_FOLDER)
                .setSinglePick(new FilePicker.OnSinglePickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void pick(@NonNull File path) throws IOException, DocumentException {
                        StringBuilder filestring = new StringBuilder("多选：\n");
                        mList = getAllDataFileName(path.getAbsolutePath());

                        for (int i = 0; i < mList.size(); i++) {
                            filestring.append(mList.get(i)).append("\n\n");
                        }
                        tvResult.setText(filestring.toString());

                        toPDF(path);
                    }
                    @Override
                    public void cancel() {
                        tvResult.setText("取消选择了");
                    }
                })
                .open();;
    }


    public ArrayList<String> getAllDataFileName(String collectionPath){

        ArrayList<String> fileList = new ArrayList<>();
//        File file = new File(collectionPath);

        List<File> files = Arrays.asList(new File(collectionPath).listFiles());
        Collections.sort(files, new Comparator< File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (File f:files) {
            System.out.println(f.getName());
            String fileName = collectionPath+"/"+f.getName();
            if (fileName.endsWith("jpg")){
                // 文件大小
                // String fileSize = FileSizeUtil.getAutoFileOrFilesSize(tempList[i].toString());
                fileList.add(fileName);
            }
        }
        return fileList;
    }

    public String toPDF(File PDFpath) {
        Log.d(TAG, "toPDF: PDFpath"+PDFpath);
        Log.d(TAG, "toPDF: PDFpath.getName()"+PDFpath.getName());
        File file = new File(PdfUtils.ADDRESS);
        if (!file.exists())
            file.mkdirs();
        Log.d(TAG, "toPDF: debug2");
        final String pdf_address =PDFpath.getPath()+".pdf";

        Log.d(TAG, "toPDF: debug3");
        handler.sendEmptyMessage(PDF_SAVE_START);
        Log.d(TAG, "toPDF: debug4");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "toPDF: debug5");
                    Log.d(TAG, "run: pdf_address："+pdf_address);
                    pdfItextUtil = new PdfItextUtil(pdf_address);
                    //判断是否有图片没有土司提示选择图片
                    //如果有进行合成
                    if (mList.size() > 0) {
                        Log.d(TAG, "run: mList.size："+mList.size());

                        for (int i = 0; i < mList.size(); i++) {
                            Log.d(TAG, "run: mList"+mList.get(i).toString());
                            Bitmap bitmap= BitmapFactory.decodeStream(new FileInputStream(mList.get(i)));
                            //这里当然可以输入文字和标题之类的。我们项目里面是只有图片所以。只需要.addImageToPdfCenterH();当然这里的图片在pdf中的放置可以通过设置的。看工具类
                            int ret = pdfItextUtil.addImageToPdfCenterH(mList.get(i), PageSize.A4.getWidth(), PageSize.A4.getHeight());
                            if(ret != 0){
                                Toast.makeText(MainActivity.this, "图片保存异常，请重试", Toast.LENGTH_SHORT).show();
                            }
                            Message msg = new Message();
                            msg.what = PDF_SAVE_PROGRESS;
                            msg.arg1 = 100*i/mList.size();
                            handler.sendMessage(msg);

//                            handler.sendEmptyMessage(PDF_SAVE_ERROR);
                            // pdfItextUtil.addTitleToPdf("我是标题"+i).addImageToPdfCenterH(mList.get(i), PageSize.A4.getWidth() - 20, PageSize.A4.getWidth() /bitmap.getWidth() * bitmap.getHeight()).addTextToPdf("这是pdf文件的内容，我这里进行了集成这个Demo，希望可以帮助项目中遇到pdf以及文字合成，我尽自己所能,希望编程路上可以帮助到出来匝道的萌新们");
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "请选选择正确的书籍图片目录", Toast.LENGTH_SHORT).show();
                    }


                    int ret = pdfItextUtil.storeOutline(PDFpath.getPath());

                    if(ret != 0){
                        Toast.makeText(MainActivity.this, "目录保存异常，请重试", Toast.LENGTH_SHORT).show();
                    }

                    pdfItextUtil.close();



                } catch (Exception e) {
                    handler.sendEmptyMessage(PDF_SAVE_ERROR);
                    e.printStackTrace();
                } finally {
                    if (pdfItextUtil != null) {
                        pdfItextUtil.close();
                    }
                    handler.sendEmptyMessage(PDF_SAVE_RESULT);
                }
            }
        }).start();

        return pdf_address;
    }

    public String toPDF(String PDFpath) {
        Log.d(TAG, "toPDF: PDFpath"+PDFpath);
        Log.d(TAG, "toPDF: PDFpath.getName()"+PDFpath);
        File file = new File(PdfUtils.ADDRESS);
        if (!file.exists())
            file.mkdirs();
        Log.d(TAG, "toPDF: debug2");


        StringBuilder filestring = new StringBuilder("多选：\n");
        mList = getAllDataFileName(PDFpath);

        for (int i = 0; i < mList.size(); i++) {
            filestring.append(mList.get(i)).append("\n\n");
        }
        tvResult.setText(filestring.toString());


        String PDFpath1 = PDFpath.substring(0,PDFpath.length()-1);
        final String pdf_address =PDFpath1+".pdf";

        Log.d(TAG, "toPDF: debug3");
        handler.sendEmptyMessage(PDF_SAVE_START);
        Log.d(TAG, "toPDF: debug4");
        new Thread(new Runnable() {
            @Override
            public void run() {
                    Log.d(TAG, "toPDF: debug5");
                    Log.d(TAG, "run: pdf_address：" + pdf_address);
                try {
                    pdfItextUtil = new PdfItextUtil(pdf_address);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //判断是否有图片没有土司提示选择图片
                    //如果有进行合成
                    if (mList.size() > 0) {
                        Log.d(TAG, "run: mList.size：" + mList.size());

                        for (int i = 0; i < mList.size(); i++) {
                            Log.d(TAG, "run: mList" + mList.get(i).toString());
                            try {
                                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(mList.get(i)));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            //这里当然可以输入文字和标题之类的。我们项目里面是只有图片所以。只需要.addImageToPdfCenterH();当然这里的图片在pdf中的放置可以通过设置的。看工具类
                            int ret = pdfItextUtil.addImageToPdfCenterH(mList.get(i), PageSize.A4.getWidth(), PageSize.A4.getHeight());
                            Message msg = new Message();
                            msg.what = PDF_SAVE_PROGRESS;
                            msg.arg1 = 100 * i / mList.size();
                            handler.sendMessage(msg);
                        }

                        pdfItextUtil.storeOutline(PDFpath);
                        pdfItextUtil.close();
                        handler.sendEmptyMessage(PDF_SAVE_RESULT);
                    }
            }
        }).start();

        return pdf_address;
    }

    private void checkNeedPermissions() {
        //6.0以上需要动态申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //多个权限一起申请
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }

}
