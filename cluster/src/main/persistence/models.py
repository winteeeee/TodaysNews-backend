# coding: utf-8
from sqlalchemy import BigInteger, CHAR, Column, DateTime, ForeignKey, Integer
from sqlalchemy.orm import relationship
from sqlalchemy.dialects.mysql import LONGTEXT, MEDIUMTEXT
from sqlalchemy.ext.declarative import declarative_base

from src.main.persistence.custom_orm.customtypes import Vector, PyObject

Base = declarative_base()
metadata = Base.metadata


class Section(Base):
    __tablename__ = 'section'

    section_id = Column(BigInteger, primary_key=True)
    section_name = Column(CHAR(20), nullable=False, unique=True)


class Article(Base):
    __tablename__ = 'article'

    article_id = Column(BigInteger, primary_key=True)
    regdate = Column(DateTime, nullable=False)
    img_url = Column(CHAR(130), nullable=False)
    url = Column(CHAR(130), nullable=False)
    press = Column(CHAR(20), nullable=False)
    title = Column(CHAR(150), nullable=False)
    content = Column(LONGTEXT, nullable=False)
    writer = Column(CHAR(100), nullable=False)
    section_id = Column(ForeignKey('section.section_id'), nullable=False)
    cluster_id = Column(ForeignKey('cluster.cluster_id'), index=True)

    cluster = relationship('Cluster', primaryjoin='Article.cluster_id == Cluster.cluster_id')


class Cluster(Base):
    __tablename__ = 'cluster'

    cluster_id = Column(BigInteger, primary_key=True)
    regdate = Column(DateTime, nullable=False)
    img_url = Column(CHAR(130), nullable=False)
    size = Column(Integer, nullable=False)
    title = Column(CHAR(150), nullable=False)
    words = Column(CHAR(30), nullable=False)
    summary = Column(MEDIUMTEXT, nullable=False)
    section_id = Column(ForeignKey('section.section_id'), nullable=False, index=True)
    centroid_id = Column(ForeignKey('article.article_id'), nullable=False, index=True)
    related_cluster_id = Column(ForeignKey('cluster.cluster_id', ondelete='SET NULL', onupdate='SET NULL'), index=True)

    centroid = relationship('Article', primaryjoin='Cluster.centroid_id == Article.article_id')
    related_cluster = relationship('Cluster', remote_side=[cluster_id])
    section = relationship('Section')

class HotCluster(Base):
    __tablename__ = 'hot_cluster'

    cluster_id = Column(ForeignKey('cluster.cluster_id', ondelete='CASCADE', onupdate='CASCADE'), primary_key=True)
    regdate = Column(DateTime, nullable=False)
    size = Column(Integer, nullable=False)
    room_name = Column(CHAR(45), nullable=False)


class RelatedCluster(Base):
    __tablename__ = 'related_cluster'

    related_cluster_id = Column(BigInteger, primary_key=True)
    parent_cluster_id = Column(ForeignKey('cluster.cluster_id'), nullable=False, index=True)
    child_cluster_id = Column(ForeignKey('cluster.cluster_id'), nullable=False, index=True)

class PreprocessedArticle(Base):
    __tablename__ = 'preprocessed_article'

    article_id = Column(ForeignKey('article.article_id', ondelete='CASCADE', onupdate='CASCADE'), primary_key=True)
    tokens = Column(PyObject, nullable=False)
    embedding = Column(Vector, nullable=False)
    lead = Column(MEDIUMTEXT, nullable=False)
    summary = Column(MEDIUMTEXT, nullable=False)