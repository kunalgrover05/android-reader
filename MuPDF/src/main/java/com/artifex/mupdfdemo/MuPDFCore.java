package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

public class MuPDFCore {
    static {
        System.loadLibrary("mupdf");
    }

    /* Readable members */
    public int numPages = -1;
    private float pageWidth;
    private float pageHeight;
    private long globals;
    private String file_format;
    public float ratio;

    /* The native functions */
    private native long openFile(String filename);
    private native long openBuffer(String magic);
    private native String fileFormatInternal();
    private native boolean isUnencryptedPDFInternal();
    private native int countPagesInternal();
    private native void gotoPageInternal(int localActionPageNum);
    private native float getPageWidth();
    private native float getPageHeight();
    private native void drawPage(Bitmap bitmap,
                                 int pageW, int pageH,
                                 int patchX, int patchY,
                                 int patchW, int patchH,
                                 long cookiePtr);
    private native void updatePageInternal(Bitmap bitmap,
                                           int page,
                                           int pageW, int pageH,
                                           int patchX, int patchY,
                                           int patchW, int patchH,
                                           long cookiePtr);
    private native RectF[] searchPage(String text);
    private native byte[] textAsHtml();
    private native void addMarkupAnnotationInternal(PointF[] quadPoints, int type);
    private native void addInkAnnotationInternal(PointF[][] arcs);
    private native void deleteAnnotationInternal(int annot_index);
    private native int passClickEventInternal(int page, float x, float y);
    private native void setFocusedWidgetChoiceSelectedInternal(String [] selected);
    private native String [] getFocusedWidgetChoiceSelected();
    private native String [] getFocusedWidgetChoiceOptions();
    private native int getFocusedWidgetSignatureState();
    private native String checkFocusedSignatureInternal();
    private native boolean signFocusedSignatureInternal(String keyFile, String password);
    private native int setFocusedWidgetTextInternal(String text);
    private native String getFocusedWidgetTextInternal();
    private native int getFocusedWidgetTypeInternal();
    private native RectF[] getWidgetAreasInternal(int page);
    private native boolean hasOutlineInternal();
    private native boolean needsPasswordInternal();
    private native boolean authenticatePasswordInternal(String password);
    private native void startAlertsInternal();
    private native void stopAlertsInternal();
    private native void destroying();
    private native boolean hasChangesInternal();
    private native void saveInternal();
    private native long createCookie();
    private native void destroyCookie(long cookie);
    private native void abortCookie(long cookie);
    public native boolean javascriptSupported();

    public class Cookie
    {
        private final long cookiePtr;

        public Cookie()
        {
            cookiePtr = createCookie();
            if (cookiePtr == 0)
                throw new OutOfMemoryError();
        }

        public void abort()
        {
            abortCookie(cookiePtr);
        }

        public void destroy()
        {
            // We could do this in finalize, but there's no guarantee that
            // a finalize will occur before the muPDF context occurs.
            destroyCookie(cookiePtr);
        }
    }

    public MuPDFCore(String filename) throws Exception
    {
        globals = openFile(filename);
        if (globals == 0)
        {
            throw new Exception("Cannot open "+ filename);
        }
        file_format = fileFormatInternal();
        numPages = countPagesInternal();
        gotoPageInternal(0);
        ratio = getPageWidth()/getPageHeight();
    }

    public synchronized void drawPage(Bitmap bm, int page,
                                      int pageW, int pageH,
                                      int patchX, int patchY,
                                      int patchW, int patchH,
                                      Cookie cookie) {
        gotoPageInternal(page);
        drawPage(bm, pageW, pageH, patchX, patchY, patchW, patchH, cookie.cookiePtr);
    }

    public synchronized void updatePage(Bitmap bm, int page,
                                        int pageW, int pageH,
                                        int patchX, int patchY,
                                        int patchW, int patchH,
                                        Cookie cookie) {
        updatePageInternal(bm, page, pageW, pageH, patchX, patchY, patchW, patchH, cookie.cookiePtr);
    }
}
