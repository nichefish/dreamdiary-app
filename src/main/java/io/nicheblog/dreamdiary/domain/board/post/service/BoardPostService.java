package io.nicheblog.dreamdiary.domain.board.post.service;

import io.nicheblog.dreamdiary.domain.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.domain.board.post.mapstruct.BoardPostMapstruct;
import io.nicheblog.dreamdiary.domain.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.domain.board.post.repository.jpa.BoardPostRepository;
import io.nicheblog.dreamdiary.domain.board.post.spec.BoardPostSpec;
import io.nicheblog.dreamdiary.extension.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.extension.clsf.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BoardPostService
 * <pre>
 *  게시판 게시물 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("boardPostService")
@RequiredArgsConstructor
@Log4j2
public class BoardPostService
        implements BaseClsfService<BoardPostDto, BoardPostDto, Integer, BoardPostEntity> {

    @Getter
    private final BoardPostRepository repository;
    @Getter
    private final BoardPostSpec spec;
    @Getter
    private final BoardPostMapstruct mapstruct = BoardPostMapstruct.INSTANCE;

    public BoardPostMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public BoardPostMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final DtlCdService dtlCdService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 목록 Page<Entity> -> Page<Dto> 변환 (override)
     *
     * @param entityPage 페이징 처리된 Entity 목록
     * @return {@link Page} -- 변환된 페이징 처리된 Dto 목록
     */
    @Override
    public Page<BoardPostDto> pageEntityToDto(final Page<BoardPostEntity> entityPage) throws Exception {
        final List<BoardPostDto> dtoList = new ArrayList<>();
        int i = 0;
        for (BoardPostEntity entity : entityPage.getContent()) {
            final BoardPostDto listDto = mapstruct.toDto(entity);
            listDto.setRnum(CmmUtils.getPageRnum(entityPage, i));
            final String ctgrNm = dtlCdService.getDtlCdNm(listDto.getCtgrClCd(), listDto.getCtgrCd());
            listDto.setCtgrNm(ctgrNm);
            dtoList.add(listDto);
            i++;
        }

        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    /**
     * 게시판 > 게시판 상단 고정 목록 조회
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 상단 고정 게시물 목록
     */
    public List<BoardPostDto> getFxdList(final String contentType) throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("contentType", contentType);
            put("fxdYn", "Y");
        }};

        final List<BoardPostEntity> entityList = this.getListEntity(searchParamMap);
        final List<BoardPostDto> dtoList = new ArrayList<>();
        for (BoardPostEntity entity : entityList) {
            final BoardPostDto listDto = mapstruct.toDto(entity);
            final String ctgrNm = dtlCdService.getDtlCdNm(listDto.getCtgrClCd(), listDto.getCtgrCd());
            listDto.setCtgrNm(ctgrNm);
            dtoList.add(listDto);
        }

        return dtoList;
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final BoardPostDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyNoticeReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * default: 상세 페이지 조회
     *
     * @param key 조회수를 증가시킬 항목의 키
     * @return Dto -- 조회된 객체
     */
    @Transactional
    public BoardPostDto viewDtlPage(final Integer key) throws Exception {

        // 조회수 증가
        // this.hitCntUp(key);

        return this.getDtlDto(key);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final BoardPostDto postDto, final BoardPostDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getClsfKey()));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyNoticeReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final BoardPostDto deletedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new TagProcEvent(this, deletedDto.getClsfKey()));
    }

    /**
     * 게시판 > 게시판 조회 (dto level) (override)
     *
     * @param key 글 번호와 컨텐츠 타입을 포함하는 복합키 객체
     */
    @Override
    @Transactional(readOnly = true)
    public BoardPostDto getDtlDto(final Integer key) throws Exception {
        final BoardPostEntity retrievedEntity = this.getDtlEntity(key);       // Entity 레벨 조회
        final BoardPostDto retrievedDto = mapstruct.toDto(retrievedEntity);
        final String ctgrNm = dtlCdService.getDtlCdNm(retrievedDto.getCtgrClCd(), retrievedDto.getCtgrCd());
        retrievedDto.setCtgrNm(ctgrNm);

        return retrievedDto;
    }
}