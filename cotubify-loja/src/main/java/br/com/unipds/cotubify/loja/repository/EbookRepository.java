package br.com.unipds.cotubify.loja.repository;

import br.com.unipds.cotubify.loja.entity.Ebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EbookRepository extends JpaRepository<Ebook, Long> {

    @Query("SELECT e FROM Ebook e LEFT JOIN FETCH e.authors")
    List<Ebook> findAllWithAuthors();

}