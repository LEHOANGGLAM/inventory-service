package com.yes4all.repository;

import com.yes4all.domain.IssueNote;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the IssueNote entity.
 */
@SuppressWarnings("unused")
@Repository
public interface IssueNoteRepository extends JpaRepository<IssueNote, Long> {
    @Query(
        value = "SELECT isn.*" +
        " FROM issue_note isn" +
        " WHERE" +
        "     isn.is_active = true" +
        "     AND isn.warehouse_from_id = :warehouseId" +
        "     AND (CASE" +
        "              WHEN :searchType = 'issueType' THEN UPPER(isn.issue_type) IN :searchList" +
        "              WHEN :searchType = 'status' THEN UPPER(isn.status) IN :searchList" +
        "              WHEN :searchType = 'channel' THEN UPPER(isn.channel) IN :searchList" +
        "              WHEN :searchType = 'issueCodeLike' THEN UPPER(isn.issue_code) LIKE CONCAT('%', UPPER(:issueCodeLike), '%')" +
        "              WHEN :searchType = 'issueCodeIn' THEN UPPER(isn.issue_code) IN :listIssueCodeIn" +
        "              WHEN :searchType = 'sku' THEN isn.id IN :listId" +
        "              WHEN :searchType = 'createdDate' THEN isn.created_date BETWEEN :fromDate AND :toDate" +
        "              WHEN :searchType = 'issueDate' THEN isn.issue_date BETWEEN :fromDate AND :toDate" +
        "              ELSE '1' = '1'" +
        "         END)" +
        "     AND (CASE" +
        "              WHEN :flgAll = true THEN '1' = '1'" +
        "              ELSE isn.issue_code IN :listIssueCode" +
        "         END)",
        nativeQuery = true
    )
    Page<IssueNote> findByCondition(
        @Param("issueCodeLike") String issueCodeLike,
        @Param("listIssueCodeIn") List<String> listIssueCodeIn,
        @Param("searchList") List<String> searchList,
        @Param("listId") List<Integer> listId,
        @Param("searchType") String searchType,
        @Param("warehouseId") Long warehouseId,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("flgAll") boolean flgAll,
        @Param("listIssueCode") List<String> listIssueCode,
        Pageable pageable
    );

    @Query(
        value = "SELECT isn.id" +
        " FROM issue_note isn" +
        " INNER JOIN issue_item item ON isn.id = item.issue_note_id" +
        " INNER JOIN product p ON item.product_id = p.id" +
        " WHERE UPPER(p.sku) LIKE '%'||:sku||'%'",
        nativeQuery = true
    )
    List<Integer> getListIdIssueNoteBySkuLike(@Param("sku") String sku);

    @Query(
        value = "SELECT isn.id" +
        " FROM issue_note isn" +
        " INNER JOIN issue_item item ON isn.id = item.issue_note_id" +
        " INNER JOIN product p ON item.product_id = p.id" +
        " WHERE UPPER(p.sku) IN :listSKU",
        nativeQuery = true
    )
    List<Integer> getListIdIssueNoteByListSku(@Param("listSKU") List<String> listSKU);

    Optional<IssueNote> findByIssueCode(String issueCode);

    @Query(value = "SELECT NEXTVAL('sequence_issue_note')", nativeQuery = true)
    Long getNextIssueNoteValue();

    @Modifying
    @Query(
        value = "ALTER SEQUENCE sequence_issue_note RESTART WITH 1; " +
        "ALTER SEQUENCE sequence_receipt_note RESTART WITH 1;" +
        "ALTER SEQUENCE sequence_adjustment RESTART WITH 1;",
        nativeQuery = true
    )
    void resetSequence();

    @Query(
        value = "SELECT isn.issue_code" +
        " FROM issue_note isn" +
        " LEFT JOIN receipt_note rn ON isn.issue_code = rn.issue_code " +
        " WHERE " +
        "     isn.issue_type = :issueType" +
        "     AND rn.total_actual_imported_qty is null",
        nativeQuery = true
    )
    List<String> getListCodeTransfer(@Param("issueType") String issueType);

    @Query(
        value = "SELECT i.* FROM issue_note i INNER JOIN \n" +
        "       ( SELECT il.reference_id \n" +
        "       FROM inventory_log il\n" +
        "       INNER JOIN inventory_query iq ON il.product_id = iq.product_id AND il.warehouse_id = iq.warehouse_id\n" +
        "       INNER JOIN period_log pl ON pl.id = iq.period_id \n" +
        "       INNER JOIN product p ON p.sku = :sku AND p.id = iq.product_id \n" +
        "       WHERE pl.full_date BETWEEN :fromDate AND :toDate AND iq.warehouse_id =:warehouseId \n" +
        "       GROUP BY il.reference_id) AS iFilter\n" +
        "   ON i.id = iFilter.reference_id\n" +
        "   ORDER BY i.created_date DESC",
        nativeQuery = true
    )
    Page<IssueNote> filterCompletedIssueNote(
        @Param("sku") String sku,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("warehouseId") Long warehouseId,
        Pageable pageable
    );

    boolean existsByIssueCode(String issueCode);
}
