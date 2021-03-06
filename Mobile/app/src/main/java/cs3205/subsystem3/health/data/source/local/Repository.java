package cs3205.subsystem3.health.data.source.local;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cs3205.subsystem3.health.common.core.JSONFileReader;
import cs3205.subsystem3.health.common.core.JSONFileWriter;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.utilities.JSONUtil;
import cs3205.subsystem3.health.model.Session;
import cs3205.subsystem3.health.model.Steps;

import static cs3205.subsystem3.health.common.core.JSONFileWriter.FOLDER;
import static cs3205.subsystem3.health.common.core.JSONFileWriter.FRONT_SLASH;

/**
 * For local repository
 * Created by Yee on 10/08/17.
 */

public class Repository {
    public static final int filePaths = 0;
    public static final int sessionNames = 1;

    public static Steps getFile(String dirPath, String fileName, String sessionName) {
        File file = new File(dirPath, fileName);
        Steps data = new Steps(0, sessionName);

        if (file.exists()) {
            try {
                JSONObject stepsJSON = JSONFileReader.toJSONObj(file.getAbsolutePath());
                return JSONUtil.JSONtoSteps(stepsJSON);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            JSONFileWriter.createFile(dirPath, fileName, JSONUtil.stepsDataToJSON(data));
        }

        return data;
    }

    public static void writeFile(Context context, String dirPath, String fileName, Steps data) {
        dirPath += (FRONT_SLASH + FOLDER);

        String filePath = dirPath + FRONT_SLASH + fileName;

        Session session = new Session(data.getTitle(), fileName, null, null);

        JSONObject jsonObject = JSONUtil.stepsDataToJSON(data);

        JSONFileWriter.toFile(context, filePath, jsonObject, session, true);
    }

    public static ArrayList<Boolean> deleteFiles(Context context, ArrayList<String> files) {
        ArrayList<Boolean> deleted = new ArrayList<Boolean>();
        for (String filePath : files) {
            File file = new File(filePath);
            deleted.add(file.delete());

            SessionDB db = new SessionDB(context);
            db.deleteSession(file.getName());
            db.close();
        }

        return deleted;
    }

    public static ArrayList<ArrayList<String>> getFiles(Context context, String dirPath) {
        ArrayList<ArrayList<String>> filesInFolder = new ArrayList<ArrayList<String>>();

        ArrayList<String> arrayListOfFilesPath = new ArrayList<String>();
        ArrayList<String> arrayListOfFileSessionNames = new ArrayList<String>();

        File f = new File(dirPath);
        f.mkdirs();
        File[] listFiles = f.listFiles();
        if (listFiles.length == 0) {
            return new ArrayList<ArrayList<String>>();
        } else {
            for (int i = 0; i < listFiles.length; i++) {
                String filePath = listFiles[i].getAbsolutePath();

                arrayListOfFilesPath.add(filePath);

//                JSONObject stepsJSON = null;
//                try {
//                    stepsJSON = JSONFileReader.toJSONObj(filePath);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                //Steps data = JSONUtil.JSONtoSteps(stepsJSON);
                //arrayListOfFileSessionNames.add(data.getTitle());

                SessionDB db = new SessionDB(context);
                Session session = db.getSession(listFiles[i].getName());
                db.close();
                arrayListOfFileSessionNames.add(session.getTitle());
            }
        }

        filesInFolder.add(arrayListOfFilesPath);
        filesInFolder.add(arrayListOfFileSessionNames);

        return filesInFolder;
    }
}
