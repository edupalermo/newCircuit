package org.circuit.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.circuit.dao.EvaluatorWrapperDao;
import org.circuit.dao.ProblemDao;
import org.circuit.entity.EvaluatorWrapper;
import org.circuit.entity.Problem;
import org.circuit.evaluator.Evaluator;
import org.circuit.utils.IoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcEvaluatorWrapperDao implements EvaluatorWrapperDao {
	
	@Autowired
	private ProblemDao problemDao;

	private JdbcTemplate jdbcTemplate;
	
	private final static String INSERT_SQL = "insert into evaluator (problem_id, object, created) values (?, ?, ?)";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	

	@Override
	public EvaluatorWrapper create(Problem problem, Evaluator evaluator) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(
		    new PreparedStatementCreator() {
		        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
		            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[] {"evaluator_id"});
		            ps.setInt(1, problem.getId());
		            ps.setString(2, IoUtils.objectToBase64(evaluator));
		            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
		            return ps;
		        }
		    },
		    keyHolder);
		
		EvaluatorWrapper evaluatorWrapper = new EvaluatorWrapper();
		evaluatorWrapper.setId(keyHolder.getKey().intValue());
		evaluatorWrapper.setEvaluator(evaluator);
		
		return evaluatorWrapper;
	}

	@Override
	public EvaluatorWrapper findLatest(Problem problem) {
    	List<EvaluatorWrapper> list = this.jdbcTemplate.query(
    	        "select problem_id, evaluator_id, object, created from evaluator where problem_id = ? order by created desc FETCH FIRST ROW ONLY",
    	        new Object[]{Integer.valueOf(problem.getId())},
    	        new RowMapper<EvaluatorWrapper>() {
    	            public EvaluatorWrapper mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	EvaluatorWrapper evaluatorWrapper = new EvaluatorWrapper();
    	            	evaluatorWrapper.setId(rs.getInt("evaluator_id"));
    	            	evaluatorWrapper.setProblem(problemDao.getById(rs.getInt("problem_id")));
    	            	evaluatorWrapper.setEvaluator(IoUtils.base64ToObject(rs.getString("object"), Evaluator.class));
    	            	evaluatorWrapper.setCreated(rs.getTimestamp("created").toLocalDateTime());
    	                return evaluatorWrapper;
    	            }
    	        });
    	
    	if (list.size() > 1) {
    		throw new RuntimeException("Inconsistency!");
    	}
    	
    	EvaluatorWrapper evaluatorWrapper = null;
    	if (list.size() > 0) {
    		evaluatorWrapper = list.get(0);
    	}
    	
    	return evaluatorWrapper;
	}

}
