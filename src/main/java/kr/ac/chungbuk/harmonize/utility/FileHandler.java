package kr.ac.chungbuk.harmonize.utility;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FileHandler {

    public static String saveAlbumCoverFile(MultipartFile file, Long musicId) throws IOException {

        String directoryPath = System.getProperty("user.dir") + "/upload/albumcover/";
        if (!new File(directoryPath).exists()) {
            new File(directoryPath).mkdirs();
        }

        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filePath = directoryPath + "/" + Objects.requireNonNull(Objects.toString(musicId)) + "." + fileExtension;

        File destFile = new File(filePath);
        file.transferTo(destFile);

        return "/api/music/albumcover/" + musicId + "." + fileExtension;
    }
}
