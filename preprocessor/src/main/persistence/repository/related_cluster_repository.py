from src.main.persistence.custom_orm import Repository
from src.main.persistence.models import RelatedCluster


class RelatedClusterRepository(Repository):
    def __init__(self):
        super().__init__(RelatedCluster)

