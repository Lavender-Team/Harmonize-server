# 하모나이즈 서버

**하모나이즈**는 사용자의 목소리를 분석하여, 부르기 좋은 노래를 추천해주는 음악 추천 서비스입니다.

![workflow](https://github.com/Lavender-Team/Harmonize-server/actions/workflows/gradle.yml/badge.svg)

## Built With

![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)

## Authors ✨

* [배재원](https://github.com/JaewonB37)
* [김성욱](https://github.com/sori9899)
* [이우창](https://github.com/changi1122)

## Documention

<details>
  <summary>API List</summary>

### [MusicController](https://github.com/Lavender-Team/Harmonize-server/blob/develop/src/main/java/kr/ac/chungbuk/harmonize/controller/MusicController.java)

|   Domain   | Index | Method | URI                     | Description     |
|:----------:|:-----:|--------|-------------------------|-----------------|
| /api/music |   1   | POST   | /                       | 음악 생성           |
|            |   2   | PUT    | /{musicId}              | 음악 수정           |
|            |   3   | DELETE | /{musicId}              | 음악 삭제           |
|            |   4   | POST   | /bulk                   | 음악 벌크 업로드       |
|            |   5   | GET    | /{musicId}              | 음악 상세 조회        |
|            |   5   | GET    | /{musicId}              | 음악 상세 조회        |
|            |   6   | GET    | /                       | 음악 목록 조회        |
|            |   7   | GET    | /search                 | 음악 상세 검색        |
|            |   8   | GET    | /rank                   | 인기곡 목록 조회       |
|            |   9   | GET    | /recent                 | 최신 음악 목록 조회     |
|            |  10   | GET    | /theme                  | 전체 테마 목록 조회     |
|            |  11   | GET    | /theme/music            | 특정 테마의 음악 목록 조회 |
|            |  12   | GET    | /count                  | 전체 음악 수 조회      |
|            |  13   | GET    | /albumcover/{filename}  | 앨범커버 파일 다운로드    |


### [MusicAnalysisController](https://github.com/Lavender-Team/Harmonize-server/blob/develop/src/main/java/kr/ac/chungbuk/harmonize/controller/MusicAnalysisController.java)

|   Domain   | Index | Method | URI                            | Description         |
|:----------:|:-----:|--------|--------------------------------|---------------------|
| /api/music |  14   | POST   | /{musicId}/files               | 음악 및 가사 파일 업로드      |
|            |  15   | POST   | /bulk/files                    | 앨범커버, 음악, 가사 벌크 업로드 |
|            |  16   | POST   | /{musicId}/analyze             | 음악 분석 요청            |
|            |  17   | PUT    | /{musicId}/delete?action=value | Pitch 값 제거 요청       |
|            |  18   | PUT    | /{musicId}/delete?action=range | Pitch 범위 제거 요청      |
|            |  19   | GET    | /audio/{filename}              | 음악 파일 다운로드          |
|            |  20   | GET    | /pitch/{musicId}               | Pitch 그래프 파일 다운로드   |


### [MusicActionController](https://github.com/Lavender-Team/Harmonize-server/blob/develop/src/main/java/kr/ac/chungbuk/harmonize/controller/MusicActionController.java)

|   Domain   | Index | Method | URI             | Description   |
|:----------:|:-----:|--------|-----------------|---------------|
| /api/music |  21   | POST   | /{musicId}/like | 북마크(좋아요)      |
|            |  22   | DELETE | /{musicId}/like | 북마크(좋아요) 취소   |
|            |  23   | GET    | /bookmarked     | 북마크한 음악 목록 조회 |


### [ArtistController](https://github.com/Lavender-Team/Harmonize-server/blob/develop/src/main/java/kr/ac/chungbuk/harmonize/controller/ArtistController.java)

|   Domain    | Index | Method | URI                 | Description     |
|:-----------:|:-----:|--------|---------------------|-----------------|
| /api/artist |  24   | POST   | /                   | 가수 등록           |
|             |  25   | PUT    | /{artistId}         | 가수 수정           |
|             |  26   | DELETE | /{artistId}         | 가수 삭제           |
|             |  27   | GET    | /                   | 가수 목록 조회        |
|             |  28   | GET    | /{artistId}         | 가수 상세 조회        |
|             |  29   | GET    | /count              | 전체 가수 수 조회      |
|             |  30   | GET    | /profile/{filename} | 프로필 이미지 파일 다운로드 |


### [GroupController](https://github.com/Lavender-Team/Harmonize-server/blob/develop/src/main/java/kr/ac/chungbuk/harmonize/controller/GroupController.java)

|   Domain   | Index | Method | URI                 | Description     |
|:----------:|:-----:|--------|---------------------|-----------------|
| /api/group |  31   | POST   | /                   | 그룹 등록           |
|            |  32   | PUT    | /{groupId}          | 그룹 수정           |
|            |  33   | DELETE | /{groupId}          | 그룹 삭제           |
|            |  34   | GET    | /                   | 그룹 목록 조회        |
|            |  35   | GET    | /{groupId}          | 그룹 상세 조회        |
|            |  36   | GET    | /profile/{filename} | 프로필 이미지 파일 다운로드 |


### [UserController](https://github.com/Lavender-Team/Harmonize-server/blob/develop/src/main/java/kr/ac/chungbuk/harmonize/controller/UserController.java)

|  Domain   | Index | Method | URI               | Description  |
|:---------:|:-----:|--------|-------------------|--------------|
| /api/user |  37   | POST   | /                 | 사용자 생성       |
|           |  38   | PUT    | /{userId}         | 사용자 수정 (사용자) |
|           |  39   | PUT    | /admin/{userId}   | 사용자 수정 (어드민) |
|           |  40   | DELETE | /{userId}         | 사용자 삭제       |
|           |  41   | GET    | /{userId}         | 사용자 상세 조회    |
|           |  42   | GET    | /                 | 사용자 목록 조회    |
|           |  43   | POST   | /login            | 로그인          |
|           |  44   | GET    | /logout           | 로그아웃         |
|           |  45   | GET    | /auth/currentuser | 로그인된 사용자 조회  |
|           |  46   | GET    | /count            | 전체 사용자 수 조회  |


### [LogController](https://github.com/Lavender-Team/Harmonize-server/blob/develop/src/main/java/kr/ac/chungbuk/harmonize/controller/LogController.java)

|  Domain  | Index | Method | URI         | Description        |
|:--------:|:-----:|--------|-------------|--------------------|
| /api/log |  47   | GET    | /bulk       | 벌크 업로드 결과 조회       |
|          |  48   | DELETE | /bulk       | 벌크 업로드 결과 삭제       |
|          |  49   | GET    | /bulk/files | 파일 벌크 업로드 결과 조회    |
|          |  50   | DELETE | /bulk/files | 파일 벌크 업로드 결과 로그 삭제 |

</details>

## Previous version

[Lavender-Team/Harmonize](https://github.com/Lavender-Team/Harmonize)
