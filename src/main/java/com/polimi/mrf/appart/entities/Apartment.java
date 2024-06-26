package com.polimi.mrf.appart.entities;

import com.google.gson.annotations.Expose;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*@NamedQuery(name="Apartment.mail",
        query="SELECT u FROM User u WHERE u.email=:email"
)*/
/*
@NamedQuery(name = "Apartment.getNewUsers", query = "SELECT u FROM User u JOIN u.likedApartments a WHERE a.id=:apartmentId AND u.id NOT IN (SELECT lu.id FROM a.likedUsers lu) AND u.id NOT IN (SELECT iu.id FROM a.ignoredUsers iu)")
@NamedQuery(name = "Apartment.getNewUsersWhoLikedMyApartments", query = "SELECT u FROM User u JOIN u.likedApartments a WHERE a IN :ownedApartmentList AND u.id NOT IN (SELECT lu.id FROM a.likedUsers lu) AND u.id NOT IN (SELECT iu.id FROM a.ignoredUsers iu)")
*/
@NamedQuery(name = "Apartment.getNewUsers", query = "SELECT new com.polimi.mrf.appart.entities.UserApartmentContainer( TREAT (u AS User), TREAT (a AS Apartment )) FROM User u JOIN u.likedApartments a WHERE (a.id=:apartmentId AND u NOT IN (SELECT m.user FROM a.matches m) AND u NOT IN (SELECT iu FROM a.ignoredUsers iu))")
@NamedQuery(name = "Apartment.getNewUsersWhoLikedMyApartments", query = "SELECT new com.polimi.mrf.appart.entities.UserApartmentContainer(u,a) FROM User u JOIN u.likedApartments a WHERE (u.bio is not null AND length(u.bio)>0 AND u.reason is not null AND length(u.reason)>0 and u.month is not null and u.job is not null and length(u.job)>0 and u.income is not null and length(u.income)>0 and u.smoker is not null and u.pets is not null and length(u.pets)>0 ) AND (a IN :ownedApartmentList AND u NOT IN (SELECT m.user FROM a.matches m) AND u NOT IN (SELECT iu FROM a.ignoredUsers iu))")

@Entity
public class Apartment {
    @Expose
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Expose
    @ManyToOne(cascade = {CascadeType.MERGE})
    private User owner;

    @Expose
    private String listingTitle;
    @Expose
    private String description;
    @Expose
    private int price;
    @Expose
    private String address;
    @Expose
    private String additionalExpenseDetail;

    @Expose
    @OneToMany(mappedBy="apartment", fetch = FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval = true)
    private List<ApartmentImage> images =new ArrayList<>();
    /*
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name="apartment_user_liked",
            joinColumns=@JoinColumn(name="apartment_id"),
            inverseJoinColumns=@JoinColumn(name="user_id"))
    private List<User> likedUsers;
     */

    //@ElementCollection(fetch = FetchType.EAGER)
    //@CollectionTable(name = "apartment_user_liked", joinColumns = @JoinColumn(name = "apartment_id"))
    //@MapKeyJoinColumn(name = "user_id", referencedColumnName = "ID")
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, mappedBy = "apartment")
    @OrderBy("matchDate DESC")
    private List<Match> matches;


    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name="apartment_user_ignored",
            joinColumns=@JoinColumn(name="apartment_id"),
            inverseJoinColumns=@JoinColumn(name="user_id"))
    private List<User> ignoredUsers;

    public List<User> getIgnoredUsers() {
        return ignoredUsers;
    }



    public void addIgnoredUser(User user) {
        this.ignoredUsers.add(user);
    }

    public void addMatch(Match match) {
        match.setApartment(this);
        this.matches.add(match);
    }

    public List<Match> getMatches() {
        return matches;
    }

    public String getAdditionalExpenseDetail() {
        return additionalExpenseDetail;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public int getPrice() {
        return price;
    }

    public void setAdditionalExpenseDetail(String additionalExpenseDetail) {
        this.additionalExpenseDetail = additionalExpenseDetail;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
        this.owner.addOwnedApartment(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Apartment)) {
            return false;
        }

        Apartment a=(Apartment) o;

        return Long.compare(a.id, this.id)==0;
    }

    public void addImage(ApartmentImage apartmentImage) {
        apartmentImage.setApartment(this);

        images.add(apartmentImage);
    }

    public boolean removeImage(long imageId) {
        //avoiding useless lookup , since equals method of Image class has been overridden, returns true if ids are the same
        ApartmentImage dummyImg=new ApartmentImage();
        dummyImg.setId(imageId);
        return this.images.remove(dummyImg);
    }


}