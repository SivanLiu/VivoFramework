package com.vivo.audiotags.generic;

import com.vivo.audiotags.AudioFile;
import com.vivo.audiotags.Tag;
import com.vivo.audiotags.exceptions.CannotWriteException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class AudioFileWriter {
    private AudioFileModificationListener modificationListener = null;

    protected abstract void deleteTag(RandomAccessFile randomAccessFile, RandomAccessFile randomAccessFile2) throws CannotWriteException, IOException;

    protected abstract void writeTag(Tag tag, RandomAccessFile randomAccessFile, RandomAccessFile randomAccessFile2) throws CannotWriteException, IOException;

    /* JADX WARNING: Removed duplicated region for block: B:71:0x013c A:{Catch:{ Exception -> 0x0148 }} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0117 A:{SYNTHETIC, Splitter: B:58:0x0117} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x011c A:{Catch:{ Exception -> 0x0186 }} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x013c A:{Catch:{ Exception -> 0x0148 }} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0117 A:{SYNTHETIC, Splitter: B:58:0x0117} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x011c A:{Catch:{ Exception -> 0x0186 }} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0129 A:{Catch:{ Exception -> 0x0186 }} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x013c A:{Catch:{ Exception -> 0x0148 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void delete(AudioFile f) throws CannotWriteException {
        Exception e;
        Throwable th;
        if (!f.canWrite()) {
            throw new CannotWriteException("Can't write to file \"" + f.getAbsolutePath() + "\"");
        } else if (f.length() <= 150) {
            throw new CannotWriteException("Less than 150 byte \"" + f.getAbsolutePath() + "\"");
        } else {
            RandomAccessFile raf = null;
            RandomAccessFile rafTemp = null;
            File file = null;
            boolean revert = false;
            try {
                file = File.createTempFile("entagged", ".tmp", f.getParentFile());
                RandomAccessFile rafTemp2 = new RandomAccessFile(file, "rw");
                File result;
                try {
                    RandomAccessFile raf2 = new RandomAccessFile(f, "rw");
                    try {
                        raf2.seek(0);
                        rafTemp2.seek(0);
                        if (this.modificationListener != null) {
                            this.modificationListener.fileWillBeModified(f, true);
                        }
                        deleteTag(raf2, rafTemp2);
                        if (this.modificationListener != null) {
                            this.modificationListener.fileModified(f, file);
                        }
                        result = f;
                        if (raf2 != null) {
                            try {
                                raf2.close();
                            } catch (Exception ex) {
                                System.err.println("AudioFileWriter:113:\"" + f.getAbsolutePath() + "\" or \"" + file.getAbsolutePath() + "\" :" + ex);
                            }
                        }
                        if (rafTemp2 != null) {
                            rafTemp2.close();
                        }
                        if (file.length() > 0) {
                            f.delete();
                            file.renameTo(f);
                            result = file;
                        } else {
                            file.delete();
                        }
                        if (this.modificationListener != null) {
                            this.modificationListener.fileOperationFinished(result);
                        }
                    } catch (Throwable veto) {
                        throw new CannotWriteException(veto);
                    } catch (Exception e2) {
                        e = e2;
                        rafTemp = rafTemp2;
                        raf = raf2;
                        revert = true;
                        try {
                            throw new CannotWriteException("\"" + f.getAbsolutePath() + "\" :" + e, e);
                        } catch (Throwable th2) {
                            th = th2;
                            result = f;
                            if (raf != null) {
                                try {
                                    raf.close();
                                } catch (Exception ex2) {
                                    System.err.println("AudioFileWriter:113:\"" + f.getAbsolutePath() + "\" or \"" + file.getAbsolutePath() + "\" :" + ex2);
                                }
                            }
                            if (rafTemp != null) {
                                rafTemp.close();
                            }
                            if (file.length() > 0 || (revert ^ 1) == 0) {
                                file.delete();
                                if (this.modificationListener != null) {
                                    this.modificationListener.fileOperationFinished(result);
                                }
                                throw th;
                            }
                            f.delete();
                            file.renameTo(f);
                            result = file;
                            if (this.modificationListener != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        rafTemp = rafTemp2;
                        raf = raf2;
                        result = f;
                        if (raf != null) {
                        }
                        if (rafTemp != null) {
                        }
                        if (file.length() > 0) {
                        }
                        file.delete();
                        if (this.modificationListener != null) {
                        }
                        throw th;
                    }
                } catch (Exception e3) {
                    e = e3;
                    rafTemp = rafTemp2;
                    revert = true;
                    throw new CannotWriteException("\"" + f.getAbsolutePath() + "\" :" + e, e);
                } catch (Throwable th4) {
                    th = th4;
                    rafTemp = rafTemp2;
                    result = f;
                    if (raf != null) {
                    }
                    if (rafTemp != null) {
                    }
                    if (file.length() > 0) {
                    }
                    file.delete();
                    if (this.modificationListener != null) {
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                revert = true;
                throw new CannotWriteException("\"" + f.getAbsolutePath() + "\" :" + e, e);
            }
        }
        return;
    }

    public synchronized void delete(RandomAccessFile raf, RandomAccessFile tempRaf) throws CannotWriteException, IOException {
        raf.seek(0);
        tempRaf.seek(0);
        deleteTag(raf, tempRaf);
    }

    public synchronized void setAudioFileModificationListener(AudioFileModificationListener listener) {
        this.modificationListener = listener;
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ea A:{Catch:{ Exception -> 0x015b }} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x014f A:{Catch:{ Exception -> 0x015b }} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x012c A:{SYNTHETIC, Splitter: B:65:0x012c} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0131 A:{Catch:{ Exception -> 0x0199 }} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x014f A:{Catch:{ Exception -> 0x015b }} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x012c A:{SYNTHETIC, Splitter: B:65:0x012c} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0131 A:{Catch:{ Exception -> 0x0199 }} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0136 A:{Catch:{ Exception -> 0x0199 }} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x014f A:{Catch:{ Exception -> 0x015b }} */
    /* JADX WARNING: Missing block: B:51:0x00f2, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void write(AudioFile af) throws CannotWriteException {
        Exception e;
        Throwable th;
        if (af.getTag().isEmpty()) {
            delete(af);
            return;
        } else if (!af.canWrite()) {
            throw new CannotWriteException("Can't write to file \"" + af.getAbsolutePath() + "\"");
        } else if (af.length() <= 150) {
            throw new CannotWriteException("Less than 150 byte \"" + af.getAbsolutePath() + "\"");
        } else {
            RandomAccessFile raf = null;
            RandomAccessFile rafTemp = null;
            File file = null;
            boolean cannotWrite = false;
            try {
                RandomAccessFile raf2;
                File result;
                file = File.createTempFile("entagged", ".tmp", af.getParentFile());
                RandomAccessFile rafTemp2 = new RandomAccessFile(file, "rw");
                try {
                    raf2 = new RandomAccessFile(af, "rw");
                } catch (Exception e2) {
                    e = e2;
                    rafTemp = rafTemp2;
                    cannotWrite = true;
                    try {
                        throw new CannotWriteException("\"" + af.getAbsolutePath() + "\" :" + e);
                    } catch (Throwable th2) {
                        th = th2;
                        result = af;
                        if (raf != null) {
                        }
                        if (rafTemp != null) {
                        }
                        if (!cannotWrite) {
                        }
                        file.delete();
                        if (this.modificationListener != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    rafTemp = rafTemp2;
                    result = af;
                    if (raf != null) {
                    }
                    if (rafTemp != null) {
                    }
                    if (cannotWrite) {
                    }
                    file.delete();
                    if (this.modificationListener != null) {
                    }
                    throw th;
                }
                try {
                    raf2.seek(0);
                    rafTemp2.seek(0);
                    if (this.modificationListener != null) {
                        this.modificationListener.fileWillBeModified(af, false);
                    }
                    writeTag(af.getTag(), raf2, rafTemp2);
                    if (this.modificationListener != null) {
                        this.modificationListener.fileModified(af, file);
                    }
                    result = af;
                    if (raf2 != null) {
                        try {
                            raf2.close();
                        } catch (Exception ex) {
                            System.err.println("AudioFileWriter:165:\"" + af.getAbsolutePath() + "\" or \"" + file.getAbsolutePath() + "\" :" + ex);
                        }
                    }
                    if (rafTemp2 != null) {
                        rafTemp2.close();
                    }
                    if (null != null || file.length() <= 0) {
                        file.delete();
                        if (this.modificationListener != null) {
                            this.modificationListener.fileOperationFinished(result);
                        }
                    } else {
                        af.delete();
                        file.renameTo(af);
                        result = file;
                        if (this.modificationListener != null) {
                        }
                    }
                } catch (Throwable veto) {
                    throw new CannotWriteException(veto);
                } catch (Exception e3) {
                    e = e3;
                    rafTemp = rafTemp2;
                    raf = raf2;
                    cannotWrite = true;
                    throw new CannotWriteException("\"" + af.getAbsolutePath() + "\" :" + e);
                } catch (Throwable th4) {
                    th = th4;
                    rafTemp = rafTemp2;
                    raf = raf2;
                    result = af;
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (Exception ex2) {
                            System.err.println("AudioFileWriter:165:\"" + af.getAbsolutePath() + "\" or \"" + file.getAbsolutePath() + "\" :" + ex2);
                        }
                    }
                    if (rafTemp != null) {
                        rafTemp.close();
                    }
                    if (cannotWrite || file.length() <= 0) {
                        file.delete();
                        if (this.modificationListener != null) {
                            this.modificationListener.fileOperationFinished(result);
                        }
                        throw th;
                    }
                    af.delete();
                    file.renameTo(af);
                    result = file;
                    if (this.modificationListener != null) {
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                cannotWrite = true;
                throw new CannotWriteException("\"" + af.getAbsolutePath() + "\" :" + e);
            }
        }
    }
}
