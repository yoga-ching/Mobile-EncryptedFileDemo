package com.example.savefile;

import static android.Manifest.permission.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKeys;

import android.Manifest;
import android.Manifest.permission;
import android.os.Bundle;
import android.os.Environment;
import android.security.keystore.KeyGenParameterSpec;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String Enc_FileName = "example_enc.txt";
    private static final String Dec_FileName = "example_dec.txt";

    //private static File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static File path = new File( Environment.getExternalStorageDirectory() + "/saved_files");
    private static EncryptedFile encryptedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button requestPermissionsBtn = findViewById(R.id.idBtnRequestPermission);
        requestPermissionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inside on click listener calling
                // method to request permission
                requestPermissions();
            }
        });



    }

    private void requestPermissions()
    {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport)
            {
                // this method is called when all permissions are granted
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    // do you work now
                    Toast.makeText(MainActivity.this, "All the permissions are granted..", Toast.LENGTH_LONG).show();

                    //continue with the EncryptedFile initialization
                    try
                    {
                        KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;
                        String mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);

                        // build the EncryptedFile class
                        encryptedFile = new EncryptedFile.Builder(
                                new File(path, Enc_FileName),
                                MainActivity.this,
                                mainKeyAlias,
                                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                        ).build();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }


                }
                // check for permanent denial of any permission
                //if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                    // permission is denied permanently,
                    // we will show user a dialog message.
                    //showSettingsDialog();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken)
            {
                // this method is called when user grants some
                // permission and denies some of them.
                permissionToken.continuePermissionRequest();
            }


            }).withErrorListener(new PermissionRequestErrorListener() {
            // this method is use to handle error
            // in runtime permissions
            @Override
            public void onError(DexterError dexterError)
            {
                // we are displaying a toast message for error message.
                Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
            }
        })
                // below line is use to run the permissions
                // on same thread and to check the permissions
                .onSameThread().check();
    }

    public void save(View v) throws IOException {

        OutputStream fos = null;
        String text = "Hello World";

        try
        {
            fos = encryptedFile.openFileOutput();
            fos.write(text.getBytes());

            //String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

            Toast.makeText(this, "Save to " + path.getAbsolutePath() + "/" + Enc_FileName, Toast.LENGTH_LONG).show();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(fos != null)
            {
                fos.flush();
                fos.close();
            }

        }
    }

    public void load(View v) throws GeneralSecurityException, IOException
    {
        FileOutputStream outputStream = null;
        try
        {
            //Read from file
            InputStream inputStream = encryptedFile.openFileInput();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int nextByte = inputStream.read();
            while (nextByte != -1) {
                byteArrayOutputStream.write(nextByte);
                nextByte = inputStream.read();
            }

            byte[] plaintext = byteArrayOutputStream.toByteArray();

            outputStream = new FileOutputStream(new File(path, Dec_FileName));
            outputStream.write(plaintext);

            Toast.makeText(this, "Save to " + path.getAbsolutePath() + "/" + Dec_FileName, Toast.LENGTH_LONG).show();


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(outputStream != null)
            {
                outputStream.flush();
                outputStream.close();
            }
        }







    }
}