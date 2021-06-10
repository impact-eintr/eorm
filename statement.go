package eorm

// 条件组装 用户API层
type Statement struct {
	clause *Clause
}

func NewStatement() *Statement {
	return &Statement{
		clause: newClause(),
	}
}

// SetTableName 设置表名
func (s *Statement) SetTableName(tableName string) *Statement {
	s.clause.tablename = tableName
	return s
}

// 新增数据API
func (s *Statement) InsertStruct(vars interface{}) *Statement {
	s.clause.insertStruct(vars)
	return s
}

// 修改数据API
func (s *Statement) UpdateStruct(vars interface{}) *Statement {
	s.clause.updateStruct(vars)
	return s
}

// where条件
func (s *Statement) AndEqual(field string, value interface{}) *Statement {
	s.clause.andEqual(field, value)
	return s
}

func (s *Statement) AndGreaterThan(field string, value interface{}) *Statement {
	s.clause.andGreaterThan(field, value)
	return s
}

func (s *Statement) AndLessThan(field string, value interface{}) *Statement {
	s.clause.andLessThan(field, value)
	return s
}

func (s *Statement) AndLike(field string, value interface{}) *Statement {
	s.clause.andLike(field, value)
	return s
}

func (s *Statement) OrEqual(field string, value interface{}) *Statement {
	s.clause.orEqual(field, value)
	return s
}

func (s *Statement) OrGreaterThan(field string, value interface{}) *Statement {
	s.clause.orGreaterThan(field, value)
	return s
}

func (s *Statement) OrLessThan(field string, value interface{}) *Statement {
	s.clause.orLessThan(field, value)
	return s
}

func (s *Statement) OrLike(field string, value interface{}) *Statement {
	s.clause.orLike(field, value)
	return s
}

// Select
func (s *Statement) Select(field ...string) *Statement {
	s.clause.selectField(field...)
	return s
}
