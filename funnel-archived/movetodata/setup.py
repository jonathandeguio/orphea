import setuptools

# with open("README.md", "r", encoding="utf-8") as fh:
#     long_description = fh.read()

setuptools.setup(
    name="movetodatafunnel",
    version="0.0.1",
    author="Rakesh Malik",
    author_email="rakesh.malik@movetodata.io",
    description="This is python movetodata decorators.",
    long_description="long_description",
    long_description_content_type="text/markdown",
    package_dir={"": "src"},
    packages=setuptools.find_packages(where="src"),
    python_requires=">=3.6",
)
