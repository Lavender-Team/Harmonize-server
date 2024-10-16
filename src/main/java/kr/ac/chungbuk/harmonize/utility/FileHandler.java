package kr.ac.chungbuk.harmonize.utility;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class FileHandler {

    /**
     * 음악의 앨범 표지를 파일시스템에 저장할 때 사용합니다.
     * 
     * @param file    저장할 앨범 표지 이미지 파일
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
     * 
     * @param albumCoverPath 음악 파일에 접근하기 위한 URL (saveAlbumCoverFile의 return 값)
     * @throws IOException
     */
    public static void deleteAlbumCoverFile(String albumCoverPath, Long musicId) throws IOException {
        String directoryPath = System.getProperty("user.dir") + "/upload/albumcover/";
        String filename = albumCoverPath.substring(albumCoverPath.indexOf(String.valueOf(musicId)));

        Files.deleteIfExists(Paths.get(directoryPath + filename));
    }

    /**
     * 음악의 오디오 파일을 파일시스템에 저장할 때 사용합니다.
     * 
     * @param audioFile 저장할 앨범 표지 이미지 파일
     * @param musicId   음악 ID
     * @return 음악 파일에 접근하기 위한 URL
     * @throws IOException
     */
    public static String saveAudioFile(MultipartFile audioFile, Long musicId) throws IOException {
        String directoryPath = System.getProperty("user.dir") + "/upload/audio/";
        if (!new File(directoryPath).exists()) {
            new File(directoryPath).mkdirs();
        }

        String fileExtension = StringUtils.getFilenameExtension(audioFile.getOriginalFilename());
        String filePath = directoryPath + "/" + Objects.requireNonNull(Objects.toString(musicId)) + "." + fileExtension;

        File destFile = new File(filePath);
        audioFile.transferTo(destFile);

        return "/api/music/audio/" + musicId + "." + fileExtension;
    }

    /**
     * 음악의 오디오 파일을 파일시스템에서 삭제할 때 사용합니다.
     * 
     * @param audioFilePath 음악 파일에 접근하기 위한 URL (saveAudioFile의 return 값)
     * @throws IOException
     */
    public static void deleteAudioFile(String audioFilePath, Long musicId) throws IOException {
        String directoryPath = System.getProperty("user.dir") + "/upload/audio/";
        String filename = audioFilePath.substring(audioFilePath.indexOf(String.valueOf(musicId)));

        Files.deleteIfExists(Paths.get(directoryPath + filename));
    }

    /**
     * 가수의 프로필 이미지를 파일시스템에 저장할 때 사용합니다.
     * 
     * @param file     저장할 프로필 이미지 파일
     * @param artistId 가수 ID
     */
    public static String saveProfileImageFile(MultipartFile file, Long artistId) throws IOException {
        String directoryPath = System.getProperty("user.dir") + "/upload/profile/";
        if (!new File(directoryPath).exists()) {
            new File(directoryPath).mkdirs();
        }

        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filePath = directoryPath + "/" + Objects.requireNonNull(Objects.toString(artistId)) + "."
                + fileExtension;

        File destFile = new File(filePath);
        file.transferTo(destFile);

        return "/api/artist/profile/" + artistId + "." + fileExtension;
    }

    /**
     * 가수의 프로필 이미지를 파일시스템에서 삭제할 때 사용합니다.
     * 
     * @param profileImagePath 가수 파일에 접근하기 위한 URL (saveProfileImageFile의 return 값)
     * @param artistId         가수 ID
     */
    public static void deleteProfileImageFile(String profileImagePath, Long artistId) throws IOException {
        String directoryPath = System.getProperty("user.dir") + "/upload/profile/";
        String filename = profileImagePath.substring(profileImagePath.indexOf(String.valueOf(artistId)));

        Files.deleteIfExists(Paths.get(directoryPath + filename));
    }

    /**
     * 그룹의 프로필 이미지를 파일시스템에 저장할 때 사용합니다.
     *
     * @param file     저장할 프로필 이미지 파일
     * @param groupId  그룹 ID
     */
    public static String saveGroupProfileImageFile(MultipartFile file, Long groupId) throws IOException {
        String directoryPath = System.getProperty("user.dir") + "/upload/group/profile/";
        if (!new File(directoryPath).exists()) {
            new File(directoryPath).mkdirs();
        }

        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filePath = directoryPath + "/" + Objects.requireNonNull(Objects.toString(groupId)) + "."
                + fileExtension;

        File destFile = new File(filePath);
        file.transferTo(destFile);

        return "/api/group/profile/" + groupId + "." + fileExtension;
    }

    /**
     * 그룹의 프로필 이미지를 파일시스템에서 삭제할 때 사용합니다.
     *
     * @param profileImagePath 그룹 프로필 이미지 파일에 접근하기 위한 URL (saveProfileImageFile의 return 값)
     * @param groupId          그룹 ID
     */
    public static void deleteGroupProfileImageFile(String profileImagePath, Long groupId) throws IOException {
        String directoryPath = System.getProperty("user.dir") + "/upload/group/profile/";
        String filename = profileImagePath.substring(profileImagePath.indexOf(String.valueOf(groupId)));

        Files.deleteIfExists(Paths.get(directoryPath + filename));
    }

    /**
     * 가수의 프로필 이미지를 복사해 그룹의 프로필 이미지로 파일시스템에 저장할 때 사용합니다.
     *
     * @param filepath 복사할 프로필 이미지 파일 경로
     * @param groupId  그룹 ID
     */
    public static String copyArtistProfileImageFile(String filepath, Long groupId, Long artistId) throws IOException {
        String directoryPath = System.getProperty("user.dir") + "/upload/group/profile/";
        if (!new File(directoryPath).exists()) {
            new File(directoryPath).mkdirs();
        }

        String artistDirectoryPath = System.getProperty("user.dir") + "/upload/profile/";
        String groupDirectoryPath = System.getProperty("user.dir") + "/upload/group/profile/";
        String filename = filepath.substring(filepath.indexOf(String.valueOf(artistId)));
        String fileExtension = StringUtils.getFilenameExtension(filename);

        Files.copy(
                Paths.get(artistDirectoryPath+filename),
                Paths.get(groupDirectoryPath+groupId.toString() + "." + fileExtension)
        );

        return "/api/group/profile/" + groupId + "." + fileExtension;
    }

    /**
     * 각 음악별로 벌크 업로드 결과를 파일로 작성합니다.
     * 
     * @param title  음악 제목
     * @param result 업로드 결과를 설명하는 문자열
     */
    public static void writeBulkUploadLog(String title, String result, boolean isFileUpload) throws IOException {
        String filename = "bulk_log.txt";
        if (isFileUpload)
            filename = "bulk_file_log.txt";

        File file = new File(System.getProperty("user.dir") + "/upload/" + filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file, true);
        BufferedWriter writer = new BufferedWriter(fw);

        writer.write(title + ": " + result + '\n');
        writer.close();
    }

    /**
     * 벌크 업로드 결과 파일의 내용을 지웁니다.
     */
    public static void clearBulkUploadLog(boolean isFileUpload) throws IOException {
        String filename = "bulk_log.txt";
        if (isFileUpload)
            filename = "bulk_file_log.txt";

        File file = new File(System.getProperty("user.dir") + "/upload/" + filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter writer = new BufferedWriter(fw);
        writer.close();
    }

    public static ResponseEntity<FileSystemResource> getFileSystemResource(String filename, String path) throws IOException {
        FileSystemResource resource = new FileSystemResource(path);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(Files.probeContentType(Path.of(path)));
        } catch (Exception e){
            mediaType = MediaType.parseMediaType("application/octet-stream");
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        new String(filename.getBytes("UTF-8"), "ISO-8859-1") + "\"")
                .body(resource);
    }
}
