package com.example.realtimedatabase;


public class Student {
    private String Name;
    private String EmailId;
    private String Password;

    public Student() {
    }

    public Student(String name,String emailId,String password) {
        this.Name = name;
        this.EmailId=emailId;
        this.Password=password;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmailId() {
        return EmailId;
    }

    public void setEmailId(String emailId) {
        EmailId = emailId;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
