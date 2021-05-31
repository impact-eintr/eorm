package eorm

import (
	"context"
	"log"
	"testing"

	_ "github.com/go-sql-driver/mysql"
)

func Newclient() (client *Client, err error) {
	setting := Settings{
		DriverName: "mysql",
		User:       "root",
		Password:   "123456",
		Database:   "sql_demo",
		Host:       "127.0.0.1:3306",
		Options:    map[string]string{"charset": "utf8mb4"},
	}
	return NewClient(setting)
}

type User struct {
	Id          int64  `eorm:"id"`
	User_id     int64  `eorm:"user_id"`
	Username    string `eorm:"username"`
	Password    string `eorm:"password"`
	Email       string `eorm:"email"`
	Gender      int    `eorm:"gender"`
	Create_time string `eorm:"create_time"`
	Update_time string `eorm:"update_time"`
}

//func TestEorm_Insert(t *testing.T) {
//	user := &User{
//		Id:          99,
//		User_id:     7345893745987349857,
//		Username:    "HelloWorld",
//		Password:    "xxx",
//		Email:       "",
//		Gender:      0,
//		Create_time: "",
//		Update_time: "",
//	}
//
//	statement := NewStatement()
//	statement = statement.SetTableName("user").InsertStruct(user)
//
//	client, _ := Newclient()
//	client.Insert(context.Background(), statement)
//}

func TestSession_FindOne(t *testing.T) {
	statement := NewStatement()
	statement = statement.SetTableName("user").
		AndEqual("username", "yixingwei").
		Select("username,User_id")
	client, err := Newclient()
	if err != nil {
		log.Println(err)
		return
	}
	user := &User{}
	_ = client.FindOne(context.Background(), statement, user)
	log.Println(user)
}
