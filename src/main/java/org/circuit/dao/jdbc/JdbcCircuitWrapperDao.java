package org.circuit.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.circuit.circuit.Circuit;
import org.circuit.dao.CircuitWrapperDao;
import org.circuit.dao.ProblemDao;
import org.circuit.entity.CircuitWrapper;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;
import org.circuit.utils.IoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCircuitWrapperDao implements CircuitWrapperDao {
	
	@Autowired
	private ProblemDao problemDao;
	
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate ;
	
	private final static String INSERT_SQL = "insert into circuit (problem_id, object, created) values (?, ?, ?)";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
	
	@Override
	public CircuitWrapper create(Problem problem, Circuit circuit) {
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(
		    new PreparedStatementCreator() {
		        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
		            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[] {"circuit_id"});
		            ps.setInt(1, problem.getId());
		            ps.setString(2, IoUtils.objectToBase64(circuit));
		            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
		            return ps;
		        }
		    },
		    keyHolder);
		
		CircuitWrapper circuitWrapper = new CircuitWrapper();
		circuitWrapper.setId(keyHolder.getKey().intValue());
		circuitWrapper.setCircuit(circuit);
		
		return circuitWrapper;
	}
	
	
	@Override
	public Circuit findByQuery(EvaluatorWrapper evaluatorWrapper, String sql) {
		
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("evaluatorId", evaluatorWrapper.getId());
		
    	List<Circuit> list = this.namedParameterJdbcTemplate.query(
    	        sql,
    	        parameters,
    	        new RowMapper<Circuit>() {
    	            public Circuit mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	Circuit circuit =  IoUtils.base64ToObject(rs.getString("object"), Circuit.class);
    	                return circuit;
    	            }
    	        });
    	
    	if (list.size() > 1) {
    		throw new RuntimeException("Inconsistency!");
    	}
    	
    	Circuit circuit = null;
    	if (list.size() > 0) {
    		circuit = list.get(0);
    	}
    	
    	return circuit;
	}


	@Override
	public List<CircuitWrapper> findWithoutGrades(EvaluatorWrapper evaluatorWrapper, TrainingSetWrapper trainingSetWrapper) {
		
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("evaluatorId", evaluatorWrapper.getId());
		parameters.addValue("trainingSetId", trainingSetWrapper.getId());
		
		String sql = "select c.* from circuit c where not exists(select 1 from grade g where g.circuit_id = c.circuit_id and g.evaluator_id = :evaluatorId and g.training_set_id = :trainingSetId) ";
		
    	List<CircuitWrapper> list = this.namedParameterJdbcTemplate.query(
    	        sql,
    	        parameters,
    	        new RowMapper<CircuitWrapper>() {
    	            public CircuitWrapper mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	CircuitWrapper circuitWrapper = new CircuitWrapper();
    	            	circuitWrapper.setId(rs.getInt("circuit_id"));
    	            	circuitWrapper.setCircuit(IoUtils.base64ToObject(rs.getString("object"), Circuit.class));
    	            	circuitWrapper.setCreated(rs.getTimestamp("created").toLocalDateTime());
    	            	circuitWrapper.setProblem(problemDao.getById(rs.getInt("problem_id")));
    	                return circuitWrapper;
    	            }
    	        });
    	
    	return list;
	}

	
}
