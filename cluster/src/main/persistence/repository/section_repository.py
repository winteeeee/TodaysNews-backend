from src.main.persistence.custom_orm import Repository
from src.main.persistence.models import Section


class SectionRepository(Repository):
    def __init__(self):
        super().__init__(Section)
