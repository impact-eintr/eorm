# eorm
一个orm


## 实例

```go
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
	Id       int64  `eorm:"id"`
	User_id  int64  `eorm:"user_id"`
	Username string `eorm:"username"`
	Password string `eorm:"password"`
}

func TestEorm_Insert(t *testing.T) {
	user := &User{
		User_id:  7345893745987349850,
		Username: "songzhichao",
		Password: "xxx",
	}

	statement := NewStatement()
	statement = statement.SetTableName("user").InsertStruct(user)

	client, _ := Newclient()
	client.Insert(context.Background(), statement)
}

func TestSession_FindOne(t *testing.T) {
	statement := NewStatement()
	statement = statement.SetTableName("user").
		AndEqual("username", "yixingwei").
		Select("user_id,username,password")

	client, err := Newclient()
	if err != nil {
		log.Println(err)
		return
	}

	user := &User{}
	_ = client.FindOne(context.Background(), statement, user)

	log.Println(user)

}

func TestSession_Delete(t *testing.T) {
	statement := NewStatement()
	statement = statement.SetTableName("user").
		AndEqual("username", "yixingwei")
	client, _ := Newclient()
	client.Delete(context.Background(), statement)
}

func TestSession_Update(t *testing.T) {
	user := &User{
		User_id:  7345893745987349850,
		Username: "songzhichao",
		Password: "szcdmm",
	}

	statement := NewStatement()
	statement = statement.SetTableName("user").
		AndEqual("username", "songzhichao").
		UpdateStruct(user)

	client, _ := Newclient()
	client.Update(context.Background(), statement)
}

func TestSession_FindAll(t *testing.T) {
	statement := NewStatement()
	statement = statement.SetTableName("user").
		AndLessThan("id", "6").
		Select("id,user_id,username,password")

	client, err := Newclient()
	if err != nil {
		log.Println(err)
		return
	}

	var user []User
	_ = client.FindAll(context.Background(), statement, &user)
	log.Println(user)
}

```
