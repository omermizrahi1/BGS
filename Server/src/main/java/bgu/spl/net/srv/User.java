package bgu.spl.net.srv;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class User {

    private final String username;
    private final String password;
    private final LocalDate birthday;
    private boolean logged;


    public User(String username, String password, String date){
        this.username=username;
        this.password = password;
        String [] birthdayString = date.split("-");
        int year = Integer.parseInt(birthdayString[2]);
        int month = Integer.parseInt(birthdayString[1]);
        int day = Integer.parseInt(birthdayString[0]);
        this.birthday = LocalDate.of(year,month,day);
        this.logged = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getBirthdayString(){
        return birthday.getDayOfMonth() + "-" + birthday.getMonth().getValue()  +
                "-" + birthday.getYear();
    }

    public boolean isLogged() {
        return logged;
    }

    public void login(){ logged = true; }

    public void logout(){ logged = false; }

    public short getAge(){
        return (short)ChronoUnit.YEARS.between(birthday,LocalDate.now());
    }

    public String toString(){
        return username;
    }

    public boolean equals(User other){
        if(!this.username.equals(other.username)){
            return false;
        }else if(!this.password.equals(other.password)){
            return false;
        }else if(!this.birthday.equals(other.birthday)){
            return false;
        }else if(this.logged != other.logged){
            return false;
        }
        return true;
    }
}
