package com.giova.service.moneystats.app.attachments.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.generic.GenericEntity;
import javax.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "IMAGE")
public class ImageEntity extends GenericEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "FILENAME", nullable = false)
  private String fileName;

  @Column(name = "CONTENT_TYPE", nullable = false)
  private String contentType;

  @Lob
  @Column(name = "BODY", nullable = false)
  private byte[] body;
}
