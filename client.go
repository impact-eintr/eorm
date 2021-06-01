package eorm

import (
	"context"
	"database/sql"
	"fmt"
	"reflect"

	"log"
)

type Settings struct {
	DriverName string

	User     string
	Password string
	Database string
	Host     string

	Options map[string]string

	MaxOpenConns int
	MaxIdleConns int

	LoggingEnabled bool
}

type Client struct {
	db      *sql.DB
	session *Session
}

func (s *Settings) DataSourceName() string {
	queryString := ""
	for key, value := range s.Options {
		queryString += key + "=" + value + "&"
	}

	ustr := fmt.Sprintf("%s:%s@tcp(%s)/%s?%s", s.User, s.Password, s.Host, s.Database, queryString)

	return ustr
}

func NewClient(setting Settings) (c *Client, err error) {
	db, err := sql.Open(setting.DriverName, setting.DataSourceName())
	if err != nil {
		log.Println(err)
		return
	}

	if err = db.Ping(); err != nil {
		log.Println(err)
		return
	}

	c = &Client{db: db}
	c.session = NewSession(db)
	log.Println("成功连接到数据库")

	return

}

func (c *Client) Close() {
	if err := c.db.Close(); err != nil {
		log.Println(err)
	}

	log.Println("成功关闭数据库连接")

}

func (c *Client) Insert(ctx context.Context, statement *Statement) (int64, error) {
	sql := statement.clause.sql
	vars := statement.clause.params
	result, err := c.session.Raw(sql, vars...).Exec()
	if err != nil {
		return 0, err
	}

	return result.RowsAffected()

}

func (c *Client) FindOne(ctx context.Context, statement *Statement, dest interface{}) (err error) {
	if reflect.TypeOf(dest).Kind() != reflect.Ptr || reflect.ValueOf(dest).IsNil() {
		return fmt.Errorf("dest is not a ptr or nil")
	}

	destSlice := reflect.Indirect(reflect.ValueOf(dest))
	destValue := reflect.ValueOf(dest).Elem()
	if destValue.Kind() != reflect.Struct {
		return fmt.Errorf("dest is not a struct")
	}

	// 拼接完整SQL语句
	createFindSQL(statement)

	// 进行与数据库交互
	rows := c.session.Raw(statement.clause.sql, statement.clause.params...).QueryRow()

	destType := reflect.TypeOf(dest).Elem()
	schema := StructForType(destType)

	// 获取指针指向的元素信息
	destVal := reflect.New(destType).Elem()

	// 结构体字段
	var values []interface{}
	for _, name := range schema.FieldNames {
		values = append(values, destVal.FieldByName(name).Addr().Interface())
	}

	if err := rows.Scan(values...); err != nil {
		log.Println(err)
		return err
	}
	destSlice.Set(destVal)
	return nil

}

func (c *Client) FindAll(ctx context.Context, statement *Statement, dest interface{}) (err error) {
	if reflect.TypeOf(dest).Kind() != reflect.Ptr || reflect.ValueOf(dest).IsNil() {
		return fmt.Errorf("dest is not a ptr or nil")
	}

	destSlice := reflect.ValueOf(dest).Elem()
	destType := destSlice.Type().Elem()

	// 拼接完整SQL语句
	createFindSQL(statement)

	// 进行与数据库交互
	rows, err := c.session.Raw(statement.clause.sql, statement.clause.params...).Query()
	if err != nil {
		return err
	}

	if destType.Kind() == reflect.Ptr {
		destType = destType.Elem()
	}

	schema := StructForType(destType)

	for rows.Next() {

		// 获取指针指向的元素信息
		destVal := reflect.New(destType).Elem()

		// 结构体字段
		var values []interface{}
		for _, name := range schema.FieldNames {
			values = append(values, destVal.FieldByName(name).Addr().Interface())
		}

		if err := rows.Scan(values...); err != nil {
			log.Println(err)
			return err
		}

		destSlice.Set(reflect.Append(destSlice, destVal))
	}

	return nil

}

func (c *Client) Delete(ctx context.Context, statement *Statement) (int64, error) {
	createDeleteSQL(statement)
	log.Println(statement.clause.params)
	res, err := c.session.Raw(statement.clause.sql, statement.clause.params...).Exec()
	if err != nil {
		return 0, err
	}
	return res.RowsAffected()
}

func (c *Client) Update(ctx context.Context, statement *Statement) (int64, error) {
	createUpdateSQL(statement)
	log.Println(statement.clause.params)

	res, err := c.session.Raw(statement.clause.sql, statement.clause.params...).Exec()
	if err != nil {
		return 0, err
	}

	return res.RowsAffected()

}

// 拼接完整SQL语句
func createConditionSQL(statement *Statement) {
	if statement.clause.condition != "" {
		statement.clause.Set(Where, "where")
		statement.clause.SetCondition(Condition, statement.clause.condition, statement.clause.params)
	}
}

// 拼接完整SQL语句
func createUpdateSQL(statement *Statement) {
	createConditionSQL(statement)
	statement.clause.Build(Update, Where, Condition)
}

// 拼接完整SQL语句
func createDeleteSQL(statement *Statement) {
	statement.clause.Set(Delete, statement.clause.tablename)
	createConditionSQL(statement)
	statement.clause.Build(Delete, Where, Condition)
}

// 拼接完整SQL语句
func createFindSQL(statement *Statement) {
	statement.clause.Set(Select, statement.clause.cselect, statement.clause.tablename)
	createConditionSQL(statement)
	statement.clause.Build(Select, Where, Condition)
}

type TxFunc func(ctx context.Context, client *Client) (interface{}, error)

// 事务支持
func (c *Client) Transaction(f TxFunc) (result interface{}, err error) {
	if err := c.session.Begin(); err != nil {
		return nil, err
	}

	defer func() {
		if p := recover(); p != nil {
			_ = c.session.Rollback()
			panic(p)
		} else if err != nil {
			_ = c.session.Rollback()
		} else {
			err = c.session.Commit()
		}
	}()

	return f(context.Background(), c)
}
