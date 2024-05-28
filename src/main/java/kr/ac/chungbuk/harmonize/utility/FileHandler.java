package kr.ac.chungbuk.harmonize.utility;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class FileHandler {

    /**
     * 음악의 앨범 표지를 파일시스템에 저장할 때 사용합니다.
     * @param file 저장할 앨범 표지 이미지 파일
     * @param musicId 음악 ID
     * @return 음악 파일에 접근하기 위한 URL
     * @throws IOException
     */
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

    /**
     * 음악의 앨범 표지를 파일시스템에서 삭제할 때 사용합니다.
     * @param albumCoverPath 음악 파일에 접근하기 위한 URL (saveAlbumCoverFile의 return 값)
     * @throws IOException
     */
    public static void deleteAlbumCoverFile(String albumCoverPath, Long musicId) throws IOException {
        String directoryPath = System.getProperty("user.dir") + "/upload/albumcover/";
        String filename = albumCoverPath.substring(albumCoverPath.indexOf(String.valueOf(musicId)));

        Files.deleteIfExists(Paths.get(directoryPath + filename));
    }
}
