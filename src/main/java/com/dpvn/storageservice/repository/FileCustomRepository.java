package com.dpvn.storageservice.repository;

import com.dpvn.sharedcore.domain.dto.PagingResponse;
import com.dpvn.sharedcore.util.StringUtil;
import com.dpvn.storageservice.domain.dto.FileDto;
import com.dpvn.storageservice.domain.entity.File;
import com.dpvn.storageservice.domain.mapper.FileMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class FileCustomRepository {
  private final FileMapper fileMapper;
  @PersistenceContext
  private EntityManager entityManager;

  public FileCustomRepository(FileMapper fileMapper) {
    this.fileMapper = fileMapper;
  }

  public PagingResponse<FileDto> search(Integer type, String filterText, Instant fromDate, Instant toDate, String fileType, Pageable pageable) {
    String TEMPLATE_SELECT = """
        SELECT *
        FROM file f
        {WHERE}
        """;
    String TEMPLATE_COUNT = """
        SELECT count(f.id)
        FROM file f
        {WHERE}
        """;

    StringBuilder whereBuilder = new StringBuilder();
    if (type != null) {
      whereBuilder.append(" AND f.source_type = :sourceType");
    }
    if (StringUtil.isNotEmpty(filterText)) {
      whereBuilder.append(" AND f.file_name LIKE :filterText");
    }
    if (fromDate != null) {
      whereBuilder.append(" AND f.created_at >= :fromDate");
    }
    if (toDate != null) {
      whereBuilder.append(" AND f.created_at <= :toDate");
    }
    if (StringUtil.isNotEmpty(fileType)) {
      whereBuilder.append(" AND f.file_mime_type = :fileType");
    }

    String where = !whereBuilder.isEmpty() ? whereBuilder.toString().trim().replaceFirst("AND", "WHERE") : "";
    String selectSql = TEMPLATE_SELECT.replace("{WHERE}", where);
    String countSql = TEMPLATE_COUNT.replace("{WHERE}", where);

    Query selectQuery = entityManager.createNativeQuery(selectSql, File.class);
    selectQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
    selectQuery.setMaxResults(pageable.getPageSize());
    Query countQuery = entityManager.createNativeQuery(countSql);

    if (type != null) {
      selectQuery.setParameter("sourceType", type);
      countQuery.setParameter("sourceType", type);
    }
    if (StringUtil.isNotEmpty(filterText)) {
      String filterTextForSql = "%" + filterText + "%";
      selectQuery.setParameter("filterText", filterTextForSql);
      countQuery.setParameter("filterText", filterTextForSql);
    }
    if (fromDate != null) {
      selectQuery.setParameter("fromDate", fromDate);
      countQuery.setParameter("fromDate", fromDate);
    }
    if (toDate != null) {
      selectQuery.setParameter("toDate", toDate);
      countQuery.setParameter("toDate", toDate);
    }
    if (StringUtil.isNotEmpty(fileType)) {
      selectQuery.setParameter("fileType", fileType);
      countQuery.setParameter("fileType", fileType);
    }

    List<File> files = selectQuery.getResultList();

    return new PagingResponse<>(pageable.getPageNumber(), pageable.getPageSize(), ((Number) countQuery.getSingleResult()).longValue(), fileMapper.toDtoList(files));
  }
}
