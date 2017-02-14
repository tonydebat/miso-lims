package uk.ac.bbsrc.tgac.miso.core.data.impl;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Synchronize;

@Entity
@Immutable
@Table(name = "PoolDerivedInfo")
@Synchronize("Pool")
public class PoolDerivedInfo {

  @Id
  private Long poolId;
  private Date lastModified;

  public Long getId() {
    return poolId;
  }

  public Date getLastModified() {
    return lastModified;
  }
}