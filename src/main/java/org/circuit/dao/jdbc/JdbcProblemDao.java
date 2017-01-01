package org.circuit.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.circuit.dao.ProblemDao;
import org.circuit.entity.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProblemDao implements ProblemDao {
	
	private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Problem findByName(String name) {
    	List<Problem> list = this.jdbcTemplate.query(
    	        "select problem_id, name from problem where name = ?",
    	        new Object[]{name},
    	        new RowMapper<Problem>() {
    	            public Problem mapRow(ResultSet rs, int rowNum) throws SQLException {
    	                Problem problem = new Problem();
    	                problem.setId(rs.getInt("problem_id"));
    	                problem.setName(rs.getString("name"));
    	                return problem;
    	            }
    	        });
    	
    	if (list.size() > 1) {
    		throw new RuntimeException("Inconsistency!");
    	}
    	
    	Problem problem = null;
    	if (list.size() > 0) {
    		problem = list.get(0);
    	}
    	
    	return problem;
    }
    
    @Override
    public Problem getById(int id) {
    	Problem problem = this.jdbcTemplate.queryForObject(
    	        "select problem_id, name from problem where problem_id = ?",
    	        new Integer[]{id},
    	        new RowMapper<Problem>() {
    	            public Problem mapRow(ResultSet rs, int rowNum) throws SQLException {
    	                Problem problem = new Problem();
    	                problem.setId(rs.getInt("problem_id"));
    	                problem.setName(rs.getString("name"));
    	                return problem;
    	            }
    	        });
    	
    	return problem;
    }
    
    
}
