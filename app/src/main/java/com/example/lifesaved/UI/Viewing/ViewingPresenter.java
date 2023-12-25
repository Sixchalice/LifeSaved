package com.example.lifesaved.UI.Viewing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.FileTypeBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.example.lifesaved.R;
import com.example.lifesaved.models.Folder;
import com.example.lifesaved.models.Image;
import com.example.lifesaved.persistence.Repository;
import com.google.firebase.database.core.utilities.Utilities;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.authoring.tracks.MP3TrackImpl;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.codecs.aac.AACDecoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import android.media.MediaCodecInfo;


public class ViewingPresenter implements Repository.MultipleImagesListener {

    private ViewingActivity view;

    private String videoFileLocation;
    private Handler endOfVideohandler;

    public ArrayList<Image> imageArrayList = new ArrayList<>();
    private String TAG = "ViewingPresenter";

    public ViewingPresenter(ViewingActivity view) {
        this.view = view;
        Repository.getInstance().setMultipleImagesListener(this);

        Repository.getInstance().readAllImagesInFolder(view.getFolder().getFid());
        view.setDefaultFields(this.imageArrayList);

        endOfVideohandler = new Handler(){

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                addAudioToVideo();
            }
        };
    }

    @Override
    public void notifydatasetchanged() {
        view.notifydatasetwaschanged();
    }

    @Override
    public void progress(String message) {
        view.display(message);
    }

    public void add() {
        Uri uri = view.getUri();
        Image img = new Image(uri);
        imageArrayList.add(img);

        Repository.getInstance().setMultipleImagesListener(this);

        Repository.getInstance().uploadMultipleImages(view.getFolder(), uri);
        view.setDefaultFields(this.imageArrayList);

    }
    public void deleteImage(Image img, Folder currentFolder) {
        Repository.getInstance().deleteImage(img, currentFolder);
        imageArrayList.remove(img);
        view.setDefaultFields(this.imageArrayList);
    }

    private void saveAudioToExternalStorage(InputStream inputStream) {
        String fileName = "audio.aac"; // The name of the file you want to save
        File root = view.getExternalFilesDir("lifesavedAudio"); // Get the root directory of the external files directory
        File file = new File(root, fileName); // Create a new File object with the desired path and filename
        try {
            if (!file.getParentFile().exists()) { // Make sure the parent directory exists
                file.getParentFile().mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            outputStream.write(buffer);
            outputStream.close();
            inputStream.close();
            Log.d(TAG, "File saved to external storage: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addSilenceAACFile(String inputPath, String outputPath, int durationSeconds){
        String cmd = " -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -t " + durationSeconds + " -c:a aac -shortest " + outputPath;
        FFmpegKit.execute(cmd);
    }

    public void trimAACFile(String inputPath, String outputPath, int durationSeconds) {
        String cmd = " -ss " + "00:00:00.000" + " -i " + inputPath + " -t " + durationSeconds + " -c " + " copy " + outputPath;
        FFmpegKit.execute(cmd);

    }
    public void addAudioToVideo(){

        File f1 = view.getExternalFilesDir(null);
        newFolderAudio("audio");
        String path2 = f1.getAbsolutePath() + "/lifesavedAudio";
        File file = new File(path2, "audio.aac");

        InputStream inputStream = view.getResources().openRawResource(R.raw.waiting_music);

        saveAudioToExternalStorage(inputStream);

        try {
            videoFileLocation = view.GetVideoFileLocation();
            File file1 = view.getExternalFilesDir(null);
            String path = file1.getAbsolutePath() + "/lifesavedVideos";
            File _videoFile = new File(path, videoFileLocation + "temp.mp4");

            Log.e(TAG, "addAudioToVideo: " + _videoFile.getAbsolutePath());

            Movie movie = MovieCreator.build(_videoFile.getAbsolutePath());


            String audioPath = file.getAbsolutePath();
            File _aacFile = new File(audioPath);


            long mp4Length = getMp4Length(_videoFile.getAbsolutePath());
            long aacLength = getAacLength(_aacFile.getAbsolutePath());

            Log.e(TAG, "addAudioToVideo: " + "length of mp4: "+ mp4Length);
            Log.e(TAG, "addAudioToVideo: " + "length of AAC: "+ aacLength);

            File _dstFile = new File(path2, "output.aac");

            if(mp4Length < aacLength){
                Log.e(TAG, "addAudioToVideo: " + "trimming audio");

                int duration = (int) (mp4Length/1000);
                Log.e(TAG, "addAudioToVideo: " + "duration: " + duration);
                trimAACFile(_aacFile.getAbsolutePath(), _dstFile.getAbsolutePath(), duration);
            }
            else if(mp4Length > aacLength){
                int duration = (int) (mp4Length - aacLength);
                addSilenceAACFile(_aacFile.getAbsolutePath(), _dstFile.getAbsolutePath(),duration);
            }


            long newAacLength = getAacLength(_dstFile.getAbsolutePath());

            File _newAudioFile = new File(path2, "newaac.mp4");
            Log.e(TAG, "addAudioToVideo: path of new audioL " + _dstFile.getAbsolutePath());
            Log.e(TAG, "addAudioToVideo: " + "length of AAC: "+ newAacLength);

            try{
                AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(_dstFile));
                CroppedTrack aacCroppedTrack = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size());
                movie.addTrack(aacCroppedTrack);
            }catch (Exception e) {
                e.printStackTrace();
                //already exists an output file - that cant overwrite. thus we need to delete it.
                cleanUp();
            }

            //TODO: 0.25 delay breaks it here:
            Container mp4file = new DefaultMp4Builder().build(movie);

            File _outputFile = new File(path, videoFileLocation + ".mp4");

            Log.e(TAG, "addAudioToVideo: " + _outputFile.getAbsolutePath());
            FileOutputStream fileOutputStream = new FileOutputStream(_outputFile);
            FileChannel fc = fileOutputStream.getChannel();
            mp4file.writeContainer(fc);
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        view.showVideo();
        cleanUp();
    }

    private void cleanUp() {
        File ext = view.getExternalFilesDir(null);
        String videoPath = ext.getAbsolutePath() + "/lifesavedVideos";
        String audioPath = ext.getAbsolutePath() + "/lifesavedAudio";
        File _tempVideo = new File(videoPath, videoFileLocation + "temp.mp4");
        _tempVideo.delete();
        File _tempAudio = new File(audioPath, "output.aac");
        _tempAudio.delete();
    }

    private long getAacLength(String audioFilePath) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(audioFilePath);
            int trackIndex = getAudioTrackIndex(extractor);
            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);
            long durationUs = format.getLong(MediaFormat.KEY_DURATION);
            long durationMs = durationUs / 1000;
            Log.d(TAG, "Audio duration (ms): " + durationMs);
            return durationMs;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            extractor.release();
        }
        return 0;
    }
    private long getMp4Length(String videoFilePath) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(videoFilePath);
            int trackIndex = getVideoTrackIndex(extractor);
            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);
            long durationUs = format.getLong(MediaFormat.KEY_DURATION);
            long durationMs = durationUs / 1000;
            Log.d(TAG, "Video duration (ms): " + durationMs);
            return durationMs;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            extractor.release();
        }
            return 0;
    }


    private int getVideoTrackIndex(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }
    private static int getAudioTrackIndex(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public void updateImages(ArrayList<Image> images) {
        Log.e("ViewingPresenter", "updateImages: " + "I AM AT THE UPDATE-IMAGES/PRESENTER NOW");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.e("ViewingPresenter", "updateImages: " + "inside the IF of buildversion");
            images.sort((o1, o2) -> {
                if (o1.getId() == -1 || o2.getId() == -1)
                    return 0;
                if(o1.getId() < o2.getId())
                    return -1;
                else if(o1.getId() > o2.getId())
                    return 1;
                else
                return 0;
            });
        }
        imageArrayList = images;
        view.setDefaultFields(images);
    }

    public String FileName(String name) {
        name = name.replace(" ", "_");
        Log.e("viewingPresenter ", "FileName: " + name);
        return name;
    }

    private void newFolderAudio(String processName){
        FileOutputStream outputStream = null;
        File file = view.getExternalFilesDir(null);
        File dir = new File(file.getAbsolutePath() + "/lifesavedAudio");
        dir.mkdirs();

        String filename = processName;
        File outFile = new File(dir,filename);
        try{
            outputStream = new FileOutputStream(outFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            outputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        File file1 = new File(file.getAbsolutePath() + "/lifesavedAudio/" + processName);
        file1.delete();
    }

    private void newFolderVideo(String processName){
        FileOutputStream outputStream = null;
        File file = view.getExternalFilesDir(null);
        File dir = new File(file.getAbsolutePath() + "/lifesavedVideos");
        dir.mkdirs();

        String filename = processName;
        File outFile = new File(dir,filename);
        try{
            outputStream = new FileOutputStream(outFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            outputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        File file1 = new File(file.getAbsolutePath() + "/lifesavedVideos/" + processName);
        file1.delete();
    }

    class CreateVideoAsync extends AsyncTask {
        private Handler handler;
        public void setHandler(Handler handler){
            this.handler = handler;

        }
        @Override
        protected Object doInBackground(Object[] objects) {
            int size = imageArrayList.size();
            Bitmap[] bitmaps1 = new Bitmap[size];
            Log.e(TAG, "doInBackground: SIZE OF FOR LOOP:  " + size);
            for(int i = 0; i < size; i++){

                Image image = imageArrayList.get(i);
                Uri uri = image.getImgUri();
                Bitmap bitmap = null;

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(view.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e("uri", uri.toString());

                Log.e(TAG, "doInBackground: " + bitmap.toString());
                bitmaps1[i] = bitmap;
            }
            videoFileLocation = view.GetVideoFileLocation();
            newFolderVideo(videoFileLocation+"temp");
            File file1 = view.getExternalFilesDir(null);
            String path = file1.getAbsolutePath() + "/lifesavedVideos";
            FileChannelWrapper out = null;
//            File file = new File(path, videoFileLocation + ".mp4");
            File file = new File(path, videoFileLocation + "temp.mp4");

            Log.e(TAG, "doInBackground: " + file.getAbsolutePath());

            try { out = NIOUtils.writableFileChannel(file.getAbsolutePath());
                double delay = view.getDelay();
                Log.e(TAG, "doInBackground: " + delay);
                AndroidSequenceEncoder encoder;
                encoder = new AndroidSequenceEncoder(out, Rational.R(10, (int)(delay*10)));
                for (Bitmap bitmap : bitmaps1) {
                    Log.e(TAG, "doInBackground: " + bitmap.toString());
//                    bitmap = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true); // 580, 1280
                    bitmap = scaleCenterCrop(bitmap, 1000, 1000);
                    encoder.encodeImage(bitmap);
                }
                encoder.finish();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                NIOUtils.closeQuietly(out);
            }
            Log.e(TAG, "done");
            return null;
        }
        public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();

            float xScale = (float) newWidth / sourceWidth;
            float yScale = (float) newHeight / sourceHeight;
            float scale = Math.max(xScale, yScale);

            float scaledWidth = scale * sourceWidth;
            float scaledHeight = scale * sourceHeight;

            float left = (newWidth - scaledWidth) / 2;
            float top = (newHeight - scaledHeight) / 2;

            RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

            Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
            Canvas canvas = new Canvas(dest);
            canvas.drawBitmap(source, null, targetRect, null);

            return dest;
        }
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            handler.sendEmptyMessage(0);
        }
    }

    public void createVideo(View view, ArrayList<Image> imageArrayList) {

        this.imageArrayList = imageArrayList;
        ViewingPresenter.CreateVideoAsync createVideoAsync = new ViewingPresenter.CreateVideoAsync();
        createVideoAsync.setHandler(endOfVideohandler);
        Log.e(TAG, "createVideo: calling handler");
        createVideoAsync.execute();
    }
}