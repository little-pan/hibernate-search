/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.documentation.searchdsl.predicate;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.search.mapper.pojo.bridge.builtin.annotation.GeoPointBridge;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

@Entity
@Indexed
public class Author {

	@Id
	private Integer id;

	@FullTextField(analyzer = "name")
	private String firstName;

	@FullTextField(analyzer = "name")
	private String lastName;

	@Embedded
	@GeoPointBridge
	private EmbeddableGeoPoint placeOfBirth;

	@ManyToMany(mappedBy = "authors")
	private List<Book> books = new ArrayList<>();

	public Author() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public EmbeddableGeoPoint getPlaceOfBirth() {
		return placeOfBirth;
	}

	public void setPlaceOfBirth(EmbeddableGeoPoint placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}
}