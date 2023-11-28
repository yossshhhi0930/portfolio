package com.example.portfolio.entity;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.example.portfolio.entity.User;
import com.example.portfolio.entity.AbstractEntity;
import java.time.MonthDay;

import lombok.Data;

@Entity
@Table(name = "crop")
@Data
public class Crop extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @SequenceGenerator(name = "crop_id_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Column(nullable = false)
    private String name;
  
    @Column
    private String manual;
    
    @Column
    private MonthDay sowing_start;
    
    @Column
    private MonthDay sowing_end;
    
    @Column
    private MonthDay harvest_start;
    
    @Column
    private MonthDay harvest_end;
    
    @Column
    private int cultivationp_period;
    
    @ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
  
    @OneToMany
    @JoinColumn(name = "topicId", insertable = false, updatable = false)
    private List<CropImage> cropImage;
    
//  @OneToMany
//  @JoinColumn(name = "topicId", insertable = false, updatable = false)
//  private Plan plan;
    
 

//CropエンティティからUserエンティティのname属性に直接アクセスするメソッド
public String getCropImagePath() {
	
	
    return cropImage != null ? ((CropImage) cropImage).getPath() : null;
}


}
