package org.circuit.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.circuit.dao.ProblemDao;
import org.circuit.dao.TrainingSetWrapperDao;
import org.circuit.entity.Problem;
import org.circuit.entity.TrainingSetWrapper;
import org.circuit.solution.TrainingSet;
import org.circuit.utils.IoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTrainingSetWrapperDao implements TrainingSetWrapperDao {
	
	@Autowired
	private ProblemDao problemDao;

	private JdbcTemplate jdbcTemplate;
	
	private final static String INSERT_SQL = "insert into training_set (problem_id, object, created) values (?, ?, ?)";

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
	@Override
	public TrainingSetWrapper create(Problem problem, TrainingSet trainingSet) {
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(
		    new PreparedStatementCreator() {
		        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
		            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[] {"training_set_id"});
		            ps.setInt(1, problem.getId());
		            ps.setString(2, IoUtils.objectToBase64(trainingSet));
		            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
		            return ps;
		        }
		    },
		    keyHolder);
		
		TrainingSetWrapper trainingSetWrapper = new TrainingSetWrapper();
		trainingSetWrapper.setId(keyHolder.getKey().intValue());
		trainingSetWrapper.setTrainingSet(trainingSet);
		
		return trainingSetWrapper;
	}

	@Override
	public TrainingSetWrapper findLatest(Problem problem) {
    	List<TrainingSetWrapper> list = this.jdbcTemplate.query(
    	        "select problem_id, training_set_id, object, created from training_set where problem_id = ? order by created desc FETCH FIRST ROW ONLY",
    	        new Object[]{Integer.valueOf(problem.getId())},
    	        new RowMapper<TrainingSetWrapper>() {
    	            public TrainingSetWrapper mapRow(ResultSet rs, int rowNum) throws SQLException {
    	            	TrainingSetWrapper trainingSetWrapper = new TrainingSetWrapper();
    	            	trainingSetWrapper.setId(rs.getInt("training_set_id"));
    	            	trainingSetWrapper.setProblem(problemDao.getById(rs.getInt("problem_id")));
    	            	trainingSetWrapper.setTrainingSet(IoUtils.base64ToObject(rs.getString("object"), TrainingSet.class));
    	            	trainingSetWrapper.setCreated(rs.getTimestamp("created").toLocalDateTime());
    	                return trainingSetWrapper;
    	            }
    	        });
    	
    	if (list.size() > 1) {
    		throw new RuntimeException("Inconsistency!");
    	}
    	
    	TrainingSetWrapper trainingSetWrapper = null;
    	if (list.size() > 0) {
    		trainingSetWrapper = list.get(0);
    	}
    	
    	return trainingSetWrapper;
	}

}
