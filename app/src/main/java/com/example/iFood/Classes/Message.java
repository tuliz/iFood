package com.example.iFood.Classes;

/**

 * This class is the Message class
 */
public class Message {

    public String title;
    public String userImageUrl;
    public String message;
    public String toUser;
    public String sentDate;
    public String fromUser;
    public String msgID;
    public String isRead;
    public boolean isMarked=false;



    Message(){}

    public Message(String title, String userImageUrl, String message, String toUser, String sentDate, String fromUser,String msgID, String isRead) {
        this.title = title;
        this.userImageUrl = userImageUrl;
        this.message = message;
        this.toUser = toUser;
        this.sentDate = sentDate;
        this.fromUser = fromUser;
        this.msgID = msgID;
        this.isRead = isRead;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String isRead() {
        return isRead;
    }

    public void setRead(String read) {
        isRead = read;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        isMarked = marked;
    }

    @Override
    public String toString() {
        return "Message{" +
                "title='" + title + '\'' +
                ", userImageUrl='" + userImageUrl + '\'' +
                ", message='" + message + '\'' +
                ", toUser='" + toUser + '\'' +
                ", sentDate='" + sentDate + '\'' +
                ", fromUser='" + fromUser + '\'' +
                ", msgID='" + msgID + '\'' +
                ", isRead='" + isRead + '\'' +
                ", isMarked=" + isMarked +
                '}';
    }
}
