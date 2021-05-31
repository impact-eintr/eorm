package eorm

import (
	"database/sql"
	"log"
	"strings"
)

type Session struct {
	// 数据库引擎
	db *sql.DB
	tx *sql.Tx
	// SQL动态参数
	sqlValues []interface{}
	// SQL语句
	sql strings.Builder
}

// CommonDB is a minimal function set of db
type CommonDB interface {
	Query(query string, args ...interface{}) (*sql.Rows, error)
	QueryRow(query string, args ...interface{}) *sql.Row
	Exec(query string, args ...interface{}) (sql.Result, error)
}

var _ CommonDB = (*sql.DB)(nil)
var _ CommonDB = (*sql.Tx)(nil)

// DB return tx if a tx begins otherwise return *sql.DB
func (s *Session) DB() CommonDB {
	if s.tx != nil {
		return s.tx

	}
	return s.db

}

func NewSession(db *sql.DB) *Session {
	return &Session{db: db}

}

func (s *Session) Clear() {
	s.sql.Reset()
	s.sqlValues = nil

}

func (s *Session) Raw(sql string, values ...interface{}) *Session {
	s.sql.WriteString(sql)
	s.sql.WriteString(" ")
	s.sqlValues = append(s.sqlValues, values...)
	return s

}

func (s *Session) Exec() (result sql.Result, err error) {
	defer s.Clear()
	log.Println("sql执行日志: ", s.sql.String(), s.sqlValues)
	if result, err = s.DB().Exec(s.sql.String(), s.sqlValues...); err != nil {
		log.Println(err)
	}
	return

}

func (s *Session) QueryRow() *sql.Row {
	defer s.Clear()
	log.Println("sql执行日志: ", s.sql.String(), s.sqlValues)
	return s.DB().QueryRow(s.sql.String(), s.sqlValues...)

}

func (s *Session) Query() (rows *sql.Rows, err error) {
	defer s.Clear()
	log.Println("sql执行日志: ", s.sql.String(), s.sqlValues)

	if rows, err = s.DB().Query(s.sql.String(), s.sqlValues...); err != nil {
		log.Println(err)
	}

	return

}

func (s *Session) Begin() (err error) {
	log.Println("transaction begin")

	if s.tx, err = s.db.Begin(); err != nil {
		log.Println(err)
		return
	}

	return

}

func (s *Session) Commit() (err error) {
	log.Println("transaction commit")

	if err = s.tx.Commit(); err != nil {
		log.Println(err)
	}

	return

}

func (s *Session) Rollback() (err error) {
	log.Println("transaction rollback")

	if err = s.tx.Rollback(); err != nil {
		log.Println(err)
	}

	return

}
