package br.com.talison.contabil.domain;

import br.com.talison.contabil.domain.enums.EnumRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Document(collection = "user")
public class User implements Serializable {

    @Id
    private String id;

    @Field("name")
    @Indexed(unique = true)
    private String name;

    @Field("password")
    private String password;

    @Field("role")
    private EnumRole role;

    @Field("createdAt")
    private Date createdAt;

    public User() {
        this.createdAt = new Date();
    }

    public User(String name, String password, EnumRole role) {
        this();
        this.name = name;
        this.password = password;
        this.role = role;
    }

}
