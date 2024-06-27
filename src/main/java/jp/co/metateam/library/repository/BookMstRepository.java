package jp.co.metateam.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jp.co.metateam.library.model.BookMst;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface BookMstRepository extends JpaRepository<BookMst, Long> {
	List<BookMst> findAll();

	Optional<BookMst> findById(BigInteger id);

// 書籍名deletedatがnullのもの全件取得
@Query("SELECT bm FROM BookMst bm WHERE bm.deletedAt IS NULL")
 List<BookMst> findByActiveAllBook();
}