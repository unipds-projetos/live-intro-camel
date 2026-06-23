package br.com.unipds.cotubify.loja.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "author")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String miniBio;

    @ManyToMany(mappedBy = "authors")
    private Set<Ebook> ebooks = new HashSet<>();

    public Author() {
    }

    public Author(
            Long id,
            String name,
            String miniBio,
            Set<Ebook> ebooks
    ) {
        this.id = id;
        this.name = name;
        this.miniBio = miniBio;
        this.ebooks = ebooks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMiniBio() {
        return miniBio;
    }

    public void setMiniBio(String miniBio) {
        this.miniBio = miniBio;
    }

    public Set<Ebook> getEbooks() {
        return ebooks;
    }

    public void setEbooks(Set<Ebook> ebooks) {
        this.ebooks = ebooks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Author) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Author[" +
                "id=" + id + ", " +
                "name=" + name + ", " +
                "miniBio=" + miniBio + ", " +
                "ebooks=" + ebooks + ']';
    }
}
