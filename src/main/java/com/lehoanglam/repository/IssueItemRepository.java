package com.yes4all.repository;

import com.yes4all.domain.IssueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Spring Data JPA repository for the IssueItem entity.
 */
@SuppressWarnings("unused")
@Repository
public interface IssueItemRepository extends JpaRepository<IssueItem, Long> {
    Set<IssueItem> findByIssueNoteId(Long issueNoteId);

    void deleteByIssueNoteId(Long issueNoteId);
}
