package kr.ac.chungbuk.harmonize.utility;

import java.io.File;

public class FileUtils {

    /**
     * 특정 폴더 안의 모든 파일과 하위 폴더를 삭제합니다.
     *
     * @param fileDir 삭제할 폴더
     * @return 성공적으로 삭제되었는지 여부
     */
    public static boolean deleteFolderContents(File folder) {

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return false; // 유효하지 않은 폴더 처리
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 재귀적으로 하위 폴더 삭제
                    deleteFolderContents(file);
                }
                // 파일 또는 빈 폴더 삭제
                if (!file.delete()) {
                    System.err.println("Failed to delete: " + file.getAbsolutePath());
                }
            }
        }
        return true;
    }
}
