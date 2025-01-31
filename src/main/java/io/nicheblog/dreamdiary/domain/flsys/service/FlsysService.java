package io.nicheblog.dreamdiary.domain.flsys.service;

import io.nicheblog.dreamdiary.domain.flsys.model.FlsysDirDto;
import io.nicheblog.dreamdiary.domain.flsys.model.FlsysDto;
import io.nicheblog.dreamdiary.domain.flsys.model.FlsysFileDto;
import io.nicheblog.dreamdiary.domain.flsys.model.FlsysMetaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * FlsysService
 * <pre>
 *  파일시스템 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("flsysService")
@RequiredArgsConstructor
@Log4j2
public class FlsysService {

    public final FlsysMetaService flsysMetaService;

    /**
     * 경로를 받아서 파일 정보 조회
     */
    public FlsysDto getFlsysByPath(final String filePath) throws Exception {
        final Path path = Paths.get(filePath);
        final File file = path.toFile();
        final FlsysDto flsys = new FlsysDto(file);


        // TODO: 메타정보 조회
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("upperFilePath", filePath);
        }};
        final List<FlsysMetaDto> metaList = flsysMetaService.getListDto(searchParamMap);

        final File[] files = file.listFiles();
        if (files == null) return flsys;
        Arrays.sort(files, Comparator.comparing(File::getName));
        final List<FlsysDirDto> dirList = new ArrayList<>();
        for (final File f : files) {
            if (!f.isDirectory()) continue;
            final FlsysDirDto flsysDir = new FlsysDirDto(f);
            metaList.stream()
                    .filter(e -> e.getFilePath()
                                  .equals(f.getPath()
                                           .replace("\\", "/")))
                    .findFirst()
                    .ifPresent(flsysDir::setMeta);
            dirList.add(flsysDir);
        }
        flsys.setDirList(dirList);

        // 하위경로
        final List<FlsysFileDto> fileList = new ArrayList<>();
        for (final File f : files) {
            if (f.isDirectory()) continue;
            final FlsysFileDto flsysFile = new FlsysFileDto(f);
            metaList.stream()
                    .filter(e -> e.getFilePath()
                                  .equals((f.getPath()
                                            .replace("\\", "/"))))
                    .findFirst()
                    .ifPresent(flsysFile::setMeta);
            fileList.add(flsysFile);
        }
        flsys.setFileList(fileList);

        return flsys;
    }

    // public void waiting() {
    //
    //     while(true) {
    //         String path = "["+file.getPath()+"] ";
    //         System.out.print(path);
    //         request = scan.nextLine();
    //
    //         if (request.indexOf(" ")!= -1){
    //             st = new StringTokenizer(request);
    //             int num = st.countTokens();
    //             one = st.nextToken();
    //             two = st.nextToken();
    //             if (st.hasMoreTokens())
    //                 three = st.nextToken();
    //
    //             if (num == 2) {
    //
    //                 if (one.equals("cd")) {
    //                     for (int i=0; i<list.length;i++) {
    //                         File f = list[i];
    //
    //                         if (two.equals(f.getName())) {
    //                             two = file.getPath()+"\\"+two;
    //                             file = new File(two);
    //                             view();
    //                         }
    //                         else
    //                             continue;
    //                     }
    //                 }
    //                 else if (one.equals("mkdir")) {
    //
    //                     two = file.getPath()+"\\"+two;
    //                     File newFile = new File(two);
    //                     if (newFile.exists())
    //                         System.out.println("존재하는 폴더입니다.");
    //                     else {
    //                         newFile.mkdir();
    //                         System.out.println(newFile.getName()+"폴더를 생성했습니다.");
    //                     }
    //                 }
    //                 else
    //                     System.out.println("잘못된 명령어입니다.");
    //             }
    //             else if (num == 3) {
    //
    //                 if (one.equals("rename")) {
    //
    //                     three = file.getPath()+"\\"+three;
    //                     File newFile = new File(three);
    //
    //                     if (newFile.exists())
    //                         System.out.println("이미 존재하는 폴더(파일)명입니다.");
    //                     else {
    //                         two = file.getPath()+"\\"+two;
    //                         File oldFile = new File(two);
    //                         oldFile.renameTo(newFile);
    //                         System.out.println(oldFile.getName()+"을 "+newFile.getName()+"으로 변경했습니다.");
    //                     }
    //
    //                 }
    //                 else
    //                     continue;
    //
    //             }
    //             else
    //                 System.out.println("잘못된 명령어입니다.");
    //         }
    //
    //         else {
    //             if (request.equals("exit")) {
    //                 System.out.println("종료합니다.");
    //                 scan.close();
    //                 break;
    //             }
    //             else if (request.equals("dir"))
    //                 view();
    //             else if (request.equals("..")) {
    //                 String nowPath = file.getPath();
    //
    //                 if (nowPath.equals("c:\\"))
    //                     System.out.println("최상위 폴더입니다.");
    //                 else {
    //                     nowPath = nowPath.substring(0, nowPath.lastIndexOf("\\"));
    //                     if (nowPath.equals("c:"))
    //                         nowPath = nowPath+"\\";
    //                     file = new File(nowPath);
    //                     view();
    //                 }
    //             }
    //             else
    //                 continue;
    //         }
    //     }
}
