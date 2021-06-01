package eorm

import (
	"fmt"
	"reflect"
	"strings"
)

type Type int
type Operation int

const (
	Insert Type = iota
	Value
	Update
	Delete
	Limit
	Select
	Where
	Condition
)

//Clause 条款、子句
type Clause struct {
	cselect   string //查询字段
	cset      string //修改字段
	tablename string //表名

	condition string //查询条件
	limit     int32  //查询条件
	offset    int32

	sql    string //完整sql语句
	params []interface{}

	sqlType    map[Type]string
	paramsType map[Type][]interface{}
}

func newClause() *Clause {
	return &Clause{
		cselect:    "*",
		limit:      -1,
		offset:     -1,
		sqlType:    make(map[Type]string),
		paramsType: make(map[Type][]interface{}),
	}
}

func (this *Clause) SetTableName(tablename string) *Clause {
	this.tablename = tablename
	return this
}

//INSERT INTO user(name, age) VALUES("eintr", 23);
func (this *Clause) insertStruct(vars interface{}) *Clause {
	typ := reflect.TypeOf(vars)
	if typ.Kind() == reflect.Ptr {
		typ = typ.Elem()
	}
	if typ.Kind() != reflect.Struct {
		return this
	}

	//数据映射到
	schema := StructForType(typ)

	//构建SQL语句
	//INSERT INTO user(name, age)
	this.Set(Insert, this.tablename, schema.FieldNames) //["Name","Age"]

	recordValues := make([]interface{}, 0)
	recordValues = append(recordValues, schema.RecordValues(vars))

	//VALUES("eintr", 23)
	this.Set(Value, recordValues...)

	//INSERT INTO user(name, age) VALUES("eintr", 23);
	this.Build(Insert, Value)
	return this

}

//UPDATE user SET name="eintr",age=23
func (this *Clause) updateStruct(vars interface{}) *Clause {
	types := reflect.TypeOf(vars)
	if types.Kind() == reflect.Ptr {
		types = types.Elem()
	}
	if types.Kind() != reflect.Struct {
		return this
	}

	// 数据映射
	schema := StructForType(types)
	m := make(map[string]interface{})
	m = schema.UpdateParam(vars)
	fmt.Println(m)

	// 构建SQL语句
	this.Set(Update, this.tablename, m)

	return this

}

func (this *Clause) SetCondition(name Type, sql string, vars []interface{}) {
	this.sqlType[name] = sql
	this.paramsType[name] = vars

}

// 查询字段
func (this *Clause) selectField(cselect ...string) *Clause {
	this.cselect = strings.Join(cselect, ",")
	return this

}

func (this *Clause) andEqual(field string, value interface{}) *Clause {
	return this.setCondition(Condition, "AND", field, "=", value)

}

func (this *Clause) orEqual(field string, value interface{}) *Clause {
	return this.setCondition(Condition, "OR", field, "=", value)

}

//根据关键字构建sql语句
func (this *Clause) Set(operation Type, param ...interface{}) {
	sql, vars := generators[operation](param...)
	fmt.Println("拼接出的sql: ", sql)
	this.sqlType[operation] = sql
	this.paramsType[operation] = vars

}

//拼接各个sql语句
func (this *Clause) Build(orders ...Type) {
	var sqls []string
	var vars []interface{}

	for _, order := range orders {
		if sql, ok := this.sqlType[order]; ok {
			sqls = append(sqls, sql)
			vars = append(vars, this.paramsType[order]...)
		}
	}

	this.sql = strings.Join(sqls, " ")
	this.params = vars

}

// 查询条件组装
func (this *Clause) setCondition(values ...interface{}) *Clause {
	sql, vars := generators[values[0].(Type)](values[2:]...)
	this.params = append(this.params, vars...)
	this.addCondition(sql, values[1].(string))

	return this

}

// 条件组成
func (this *Clause) addCondition(sql, opt string) {
	if this.condition == "" {
		this.condition = sql
	} else {
		this.condition = fmt.Sprintf("(%v) %v (%v)", this.condition, opt, sql)
	}

}
