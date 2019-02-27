package com.vivo.media;

import android.util.Log;
import android.util.Xml;
import com.vivo.media.FeatureProject.projectFeature;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FeatureXmlReader {
    private final String FEATURE = "feature";
    private final String KEY = "key";
    private final String NAME = "name";
    private final String PROJECT = "project";
    private final String TAG = "FeatureXmlReader";
    private final String VALUE = "value";
    private final String etcPath = "/etc/afs_config.xml";
    private final String oemPath = "/oem/etc/afs_config.xml";
    private InputStream read = null;

    /* JADX WARNING: Missing block: B:25:0x009f, code:
            r14 = r15;
     */
    /* JADX WARNING: Missing block: B:26:0x00a0, code:
            r8 = r13.next();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<FeatureProject> getFeatureProjects() {
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        try {
            File fileCurrent = new File("/oem/etc/afs_config.xml");
            if (fileCurrent.exists()) {
                this.read = new FileInputStream(fileCurrent);
            } else {
                this.read = new FileInputStream("/etc/afs_config.xml");
            }
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
        }
        if (this.read == null) {
            Log.d("FeatureXmlReader", "read == null, please check paths: /etc/afs_config.xml");
            return null;
        }
        ArrayList<FeatureProject> projectLists = new ArrayList();
        FeatureProject project = null;
        BufferedInputStream buffer = new BufferedInputStream(this.read);
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] a = new byte[1024];
        while (true) {
            try {
                int n = buffer.read(a);
                if (n != -1) {
                    outSteam.write(a, 0, n);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                break;
            } catch (XmlPullParserException e4) {
                e = e4;
                e.printStackTrace();
                try {
                    outSteam.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
                try {
                    buffer.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
                return projectLists;
            } catch (IOException e5) {
                e222 = e5;
                try {
                    e222.printStackTrace();
                    try {
                        outSteam.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                    try {
                        buffer.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                    return projectLists;
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        outSteam.close();
                    } catch (IOException e222222) {
                        e222222.printStackTrace();
                    }
                    try {
                        buffer.close();
                    } catch (IOException e2222222) {
                        e2222222.printStackTrace();
                    }
                    throw th;
                }
            }
        }
        InputStream readingXML = new ByteArrayInputStream(outSteam.toByteArray());
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(readingXML, "UTF-8");
        int eventType = parser.getEventType();
        projectFeature feature = null;
        while (true) {
            FeatureProject project2 = project;
            if (eventType != 1) {
                switch (eventType) {
                    case 0:
                        project = project2;
                        break;
                    case 2:
                        try {
                            if (!parser.getName().equals("project")) {
                                if (!parser.getName().equals("name")) {
                                    if (!parser.getName().equals("feature")) {
                                        if (!parser.getName().equals("key")) {
                                            if (parser.getName().equals("value")) {
                                                eventType = parser.next();
                                                if (feature != null) {
                                                    feature.setFeatureValue(Integer.valueOf(parser.getText()).intValue());
                                                    project = project2;
                                                    break;
                                                }
                                            }
                                        }
                                        eventType = parser.next();
                                        if (feature != null) {
                                            feature.setFeatureName(parser.getText());
                                            project = project2;
                                            break;
                                        }
                                    } else if (project2 != null) {
                                        project2.getClass();
                                        feature = new projectFeature();
                                        if (feature != null) {
                                            feature.setParentName(project2.getProjectName());
                                            project = project2;
                                            break;
                                        }
                                    }
                                }
                                eventType = parser.next();
                                if (project2 != null) {
                                    project2.setProjectName(parser.getText());
                                    project = project2;
                                    break;
                                }
                            }
                            project = new FeatureProject();
                            break;
                        } catch (XmlPullParserException e6) {
                            e = e6;
                            project = project2;
                            e.printStackTrace();
                            outSteam.close();
                            buffer.close();
                            return projectLists;
                        } catch (IOException e7) {
                            e2222222 = e7;
                            project = project2;
                            e2222222.printStackTrace();
                            outSteam.close();
                            buffer.close();
                            return projectLists;
                        } catch (Throwable th3) {
                            th = th3;
                            project = project2;
                            outSteam.close();
                            buffer.close();
                            throw th;
                        }
                    case 3:
                        if (!parser.getName().equals("project")) {
                            if (parser.getName().equals("feature")) {
                                if (project2 != null) {
                                    project2.addFeature(feature);
                                }
                                feature = null;
                                project = project2;
                                break;
                            }
                        }
                        projectLists.add(project2);
                        project = null;
                        break;
                }
            }
            try {
                outSteam.close();
            } catch (IOException e22222222) {
                e22222222.printStackTrace();
            }
            try {
                buffer.close();
            } catch (IOException e222222222) {
                e222222222.printStackTrace();
            }
            project = project2;
            return projectLists;
        }
    }
}
