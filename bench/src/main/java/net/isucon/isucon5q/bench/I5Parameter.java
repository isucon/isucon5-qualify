package net.isucon.isucon5q.bench;

import java.util.List;
import java.util.ArrayList;

public class I5Parameter extends Parameter {
  private static String[] KEYS = new String[]{
    "account_name", "nick_name", "email", "password", "first_name", "last_name",
    "birthday", "sex", "pref"
  };

  public String accountName;
  public String nickName;
  public String email;
  public String password;
  public String firstName;
  public String lastName;
  public String birthday;
  public String sex;
  public String pref;

  public String[] keys() {
    return KEYS;
  }

  public void put(String name, String value) {
    switch (name) {
    case "account_name": this.accountName = value; break;
    case "nick_name":  this.nickName = value; break;
    case "email":      this.email    = value; break;
    case "password":   this.password = value; break;
    case "first_name": this.firstName = value; break;
    case "last_name":  this.lastName  = value; break;
    case "birthday":   this.birthday = value; break;
    case "sex":  this.sex = value; break;
    case "pref": this.pref = value; break;
    }
  }
}
