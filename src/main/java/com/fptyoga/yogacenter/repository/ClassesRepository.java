package com.fptyoga.yogacenter.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fptyoga.yogacenter.Entity.Class;
import com.fptyoga.yogacenter.Entity.Room;
import com.fptyoga.yogacenter.Entity.Time;

@Repository
public interface ClassesRepository extends JpaRepository<Class, Long>{

    @Query("SELECT  DISTINCT c.date FROM Class c")
    List<String> findDistinctDate();

    Page<Class> findByCourseid_Courseid(Long courseid, Pageable page);

    Page<Class> findByDate(String date, Pageable page);

    Page<Class> findByTrainerid_Userid(Long trainerid, Pageable page);

    Page<Class> findByCourseid_CourseidAndDate(Long courseId, String date, Pageable page);

    boolean existsByDateAndTimeid_Timeid(String date, Long timeid);

    @Query("SELECT COUNT(c.classid) FROM Class c WHERE c.status = true")
    int countClassesWithStatusTrue();

    boolean existsByClassname(String Classname);
    
    boolean existsByDateAndRoomidAndTimeid(String date, Room room, Time time);

    List<Class> findByStatus(boolean status);
}
