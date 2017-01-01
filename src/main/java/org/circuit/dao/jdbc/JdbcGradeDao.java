package org.circuit.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.circuit.dao.GradeDao;
import org.circuit.entity.CircuitWrapper;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.TrainingSetWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcGradeDao implements GradeDao {
	
	private static final String INSERT_SQL = "insert into grade (circuit_id, training_set_id, evaluator_id, name, value, created) values (?, ?, ?, ?, ?, ?)";
	
	private JdbcTemplate jdbcTemplate;
	
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
	@Override
	public void create(CircuitWrapper circuitWrapper, TrainingSetWrapper trainingSetWrapper, EvaluatorWrapper evaluatorWrapper, String name, int value) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(
		    new PreparedStatementCreator() {
		        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
		            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[] {"grade_id"});
		            ps.setInt(1, circuitWrapper.getId());
		            ps.setInt(2, trainingSetWrapper.getId());
		            ps.setInt(3, evaluatorWrapper.getId());
		            ps.setString(4, name);
		            ps.setInt(5, value);
		            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
		            return ps;
		        }
		    },
		    keyHolder);

	}
    


    
}
