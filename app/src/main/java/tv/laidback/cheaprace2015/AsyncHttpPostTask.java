package tv.laidback.cheaprace2015;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Upload a file to a http server in an asynchronous way.
 * An example would be
 * new AsyncHttpPostTask("http://lilldata.se/cheaprace/hub").execute(new File("movie.mp4"));
 */
public class AsyncHttpPostTask extends AsyncTask<File, Void, String> {

    private static final String TAG = AsyncHttpPostTask.class.getSimpleName();
    private String server;
    private Context context;

    public AsyncHttpPostTask(final Context c, final String server) {
        this.server = server;
        this.context = c;
    }

    @Override
    protected String doInBackground(File... params) {
        Log.d(TAG, "doInBackground");
        HttpClient http = AndroidHttpClient.newInstance("MyApp");
        HttpPost method = new HttpPost(this.server);
        method.setEntity(new FileEntity(params[0], "text/plain"));
        try {
            HttpResponse response = http.execute(method);
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            final StringBuilder out = new StringBuilder();
            String line;
            try {
                while ((line = rd.readLine()) != null) {
                    out.append(line);
                }
            } catch (Exception e) {}
            // wr.close();
            try {
                rd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // final String serverResponse = slurp(is);
            Log.d(TAG, "serverResponse: " + out.toString());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /** TODO implement this
    private File getVideo() {
        File file = new File(context.getFilesDir() + File.separator + "linaanna.mp4");
        try {
            // TODO fix valid movie InputStream inputStream = context.getResources().openRawResource(R.raw.linaanna);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte buf[]=new byte[1024];
            int len;
            InputStream is=new InputStream();
            while((len=new InputStream.read(buf))>0) {
                fileOutputStream.write(buf,0,len);
            }

            fileOutputStream.close();
            inputStream.close();

        } catch (IOException e1) {}
        return file;
    } */
}
